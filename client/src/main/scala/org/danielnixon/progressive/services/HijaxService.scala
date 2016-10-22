package org.danielnixon.progressive.services

import org.querki.jquery._
import org.scalajs.dom._
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.HTMLFormElement
import org.danielnixon.progressive.shared.Wart
import org.danielnixon.progressive.shared.api.{ AjaxResponse, Target }

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scalaz.Scalaz._

class HijaxService(
    window: Window,
    queryStringService: QueryStringService,
    historyService: HistoryService,
    userAgentService: UserAgentService,
    transitionsService: TransitionsService,
    focusManagementService: FocusManagementService,
    refreshService: RefreshService,
    enableDisableService: EnableDisableService,
    ajaxService: AjaxService,
    eventHandlerSetupService: EventHandlerSetupService,
    preFormSubmit: JQuery => Unit,
    postFormSubmit: JQuery => Unit
) {

  private def updateQueryString(params: Seq[QueryStringParam]): String = {
    val path = window.location.pathname
    val search = window.location.search
    queryStringService.updateQueryString(path, search, params)
  }

  private def getTarget(element: JQuery): Option[JQuery] = {
    val target = element.attr("data-target").toOption.filter(_.nonEmpty).flatMap(Target.fromJson)
    target map {
      case Target.Next => element.next()
      case Target.Parent => element.parent()
      case Target.ChildTarget => element.find(".target")
    }
  }

  private def focusTargetIfRequired(trigger: JQuery, target: JQuery): Unit = {
    if (!focusManagementService.anythingHasFocus) {
      val parentForm = trigger.closest("form")

      val isMissingAttr = !trigger.is("[data-focustarget]") && !parentForm.is("[data-focustarget]")
      val shouldFocusTarget = trigger.attr("data-focustarget").exists(_ == "true") || parentForm.attr("data-focustarget").exists(_ == "true")

      if (isMissingAttr || shouldFocusTarget) {
        focusManagementService.setFocus(target)
      }
    }
  }

  private def triggerRefreshIfRequired(trigger: JQuery, refreshTarget: JQuery): Unit = {
    val triggerRefresh = trigger.is("[data-trigger-refresh]") || trigger.closest("form").is("[data-trigger-refresh]")
    if (triggerRefresh) {
      refreshService.refresh(refreshTarget, userTriggered = true)
    }
  }

  def ajaxLinkClick(link: JQuery): Boolean = {

    if (!enableDisableService.isDisabled(link)) {
      val targetOpt = getTarget(link)
      val href = link.attr("href").get
      val queryStringArray = queryStringService.extractQueryStringParams(href)
      val newUri = updateQueryString(queryStringArray)
      historyService.pushState(newUri)
      val ajaxHref = link.attr("data-href").getOrElse(href)
      targetOpt.foreach(target => refreshService.updateAutoRefresh(target, ajaxHref))
      val request = ajaxService.get(ajaxHref)

      fadeOutFadeIn(request.future, link, targetOpt)
      targetOpt.foreach(target => focusManagementService.setFocus(target))
    }

    false
  }

  def ajaxSubmitButtonClick(button: JQuery): Unit = {
    val form = button.closest("form")
    val submitButtons = form.find("button[type=submit]")

    submitButtons.removeAttr("data-clicked")
    button.attr("data-clicked", "true")
  }

  // scalastyle:off method.length
  // scalastyle:off cyclomatic.complexity
  @SuppressWarnings(Array(Wart.AsInstanceOf))
  def ajaxFormSubmit(form: JQuery): Boolean = {
    val element = form(0)
    val submitButtons = form.find("button[type=submit]")

    val clickedSubmitButton = submitButtons.filter("[data-clicked]")

    if (enableDisableService.isDisabled(form) || enableDisableService.isDisabled(clickedSubmitButton)) {
      false
    } else {
      val confirmMessage = form.attr("data-confirm")
      val confirmed = confirmMessage.toOption match {
        case Some(message) => window.confirm(message)
        case None => true
      }

      if (!confirmed) {
        false
      } else {
        val confirmedAction = form.attr("data-confirmed-action")
        confirmedAction.toOption match {
          case Some(action) => form.attr("action", action)
          case None =>
        }

        val isAjax = form.attr("data-ajax").exists(_ == "true")
        if (!isAjax) {
          true
        } else {
          preFormSubmit(form)

          val action = form.attr("action").get
          val method = clickedSubmitButton.attr("formmethod").orElse(form.attr("method")).get
          val isGet = method.contains("GET")
          val serializedForm = form.serialize()

          val targetAction = {
            val clickedFormAction = clickedSubmitButton.attr("formaction")
            val ajaxAction = form.attr("data-action")
            val a = clickedFormAction.orElse(ajaxAction).getOrElse(action)
            if (isGet) queryStringService.appendQueryString(a, serializedForm) else a
          }

          val targetOpt = getTarget(if (clickedSubmitButton.is("[data-target]")) clickedSubmitButton else form)
          val refreshTarget = form.closest("[data-refresh]")

          val isSecondarySubmitButton = clickedSubmitButton.attr("formmethod").isDefined

          if (isGet && !isSecondarySubmitButton) {
            val paramsForQueryString = queryStringService.paramsForQueryString(element)
            val queryString = queryStringService.toQueryString(paramsForQueryString)
            val changingPath = action =/= window.location.pathname
            val newUri = if (changingPath) queryStringService.appendQueryString(action, queryString) else updateQueryString(paramsForQueryString)
            historyService.pushState(newUri)

            // Dismiss keyboard on iOS.
            if (userAgentService.isTouchDevice) {
              form.find("input[type=search]").blur()
            }

            targetOpt.foreach(target => refreshService.updateAutoRefresh(target, targetAction))
          }

          val isFileUpload = form.is("[enctype=\"multipart/form-data\"]")
          val formElem = element.asInstanceOf[HTMLFormElement]
          val data: Ajax.InputData = if (isFileUpload) new FormData(formElem) else serializedForm
          val headers = if (isFileUpload) Map.empty[String, String] else Map("Content-Type" -> "application/x-www-form-urlencoded")
          val request = ajaxService.ajax(method, targetAction, Some(data), headers)

          val trigger = if (clickedSubmitButton.length > 0) clickedSubmitButton else form
          val fut = fadeOutFadeIn(request.future, trigger, targetOpt)

          fut map { _ =>
            targetOpt.foreach(target => focusTargetIfRequired(trigger, target))
            triggerRefreshIfRequired(trigger, refreshTarget)
            if (isFileUpload) {
              formElem.reset()
            }
          }

          fut.onComplete(_ => postFormSubmit(form))

          false
        }
      }
    }
  }
  // scalastyle:on method.length
  // scalastyle:on cyclomatic.complexity

  private def fadeOutFadeIn(request: Future[AjaxResponse], trigger: JQuery, targetOpt: Option[JQuery]): Future[Unit] = {
    val preRender = (target: JQuery) => eventHandlerSetupService.setup(target, refreshService)

    enableDisableService.disable(trigger)
    targetOpt.foreach(target => refreshService.pauseAutoRefresh(target))

    val fut = transitionsService.fadeOutFadeIn(request, trigger, targetOpt, preRender)

    fut.onComplete { _ =>
      enableDisableService.enable(trigger)
      targetOpt.foreach(target => refreshService.resumeAutoRefresh(target))
    }

    fut
  }
}
