package org.danielnixon.progressive.services

import org.querki.jquery._
import org.scalajs.dom._
import org.danielnixon.progressive.extensions.core.StringWrapper
import org.danielnixon.progressive.extensions.dom.ElementWrapper
import org.danielnixon.progressive.extensions.jquery.JQuerySeq
import org.danielnixon.progressive.shared.Wart
import org.danielnixon.progressive.shared.api._
import org.scalajs.dom.html.{ Anchor, Button, Form }

import scala.collection.immutable.Seq
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
    preFormSubmit: JQuery => Boolean,
    postFormSubmit: JQuery => Unit
) {

  private def updateQueryString(params: Seq[QueryStringParam]): String = {
    val path = window.location.pathname
    val search = window.location.search
    queryStringService.updateQueryString(path, search, params)
  }

  private def getTarget(element: JQuery, target: Option[Target]): Option[JQuery] = {
    target map {
      case Target.Next => element.next()
      case Target.Parent => element.parent()
      case Target.ChildTarget => element.find(".target")
    }
  }

  private def focusTargetIfRequired(settings: FormSettings, target: JQuery): Unit = {
    if (!focusManagementService.anythingHasFocus && settings.focusTarget) {
      focusManagementService.setFocus(target)
    }
  }

  private def triggerRefreshIfRequired(settings: FormSettings, refreshTarget: JQuery): Unit = {
    if (settings.triggerRefresh) {
      refreshService.refresh(refreshTarget, userTriggered = true)
    }
  }

  @SuppressWarnings(Array(Wart.AsInstanceOf))
  def ajaxLinkClick(link: JQuery): Boolean = {
    val element = link(0).asInstanceOf[Anchor]

    element.getAttributeOpt("data-progressive").flatMap(LinkSettings.fromJson) foreach { settings =>

      if (!enableDisableService.isDisabled(link)) {
        val targetOpt = getTarget(link, settings.target)
        val queryStringArray = queryStringService.extractQueryStringParams(element.href)
        val newUri = updateQueryString(queryStringArray)
        historyService.pushState(newUri)
        val ajaxHref = settings.href.getOrElse(element.href)
        val request = ajaxService.get(ajaxHref)

        fadeOutFadeIn(request.future, link, targetOpt, false, None, settings.busyMessage)
        targetOpt.foreach(target => focusManagementService.setFocus(target))
      }
    }

    false
  }

  def ajaxSubmitButtonClick(button: JQuery): Unit = {
    val form = button.closest("form")
    val submitButtons = form.find("button[type=submit]")

    submitButtons.removeAttr("data-clicked")
    button.attr("data-clicked", "true")
  }

  @SuppressWarnings(Array(Wart.AsInstanceOf))
  def ajaxFormSubmit(form: JQuery): Boolean = {
    form.attr("data-progressive").toOption.flatMap(FormSettings.fromJson) match {
      case None => false
      case Some(formSettings) =>
        val clickedSubmitButton = form.find("button[type=submit][data-clicked]")
        if (enableDisableService.isDisabled(form) || enableDisableService.isDisabled(clickedSubmitButton)) {
          false
        } else {
          val confirmed = formSettings.confirmMessage.forall(window.confirm)

          if (!confirmed) {
            false
          } else {
            formSettings.confirmedAction.foreach(action => form.attr("action", action))

            if (!formSettings.ajax) {
              true
            } else {
              if (preFormSubmit(form)) {
                val fut = submitAjaxForm(form, formSettings, clickedSubmitButton)
                fut.onComplete(_ => postFormSubmit(form))
              }
              false
            }
          }
        }
    }
  }

  @SuppressWarnings(Array(Wart.AsInstanceOf))
  private def submitAjaxForm(form: JQuery, formSettings: FormSettings, clickedSubmitButton: JQuery): Future[Unit] = {
    val formElement = form(0).asInstanceOf[Form]
    val clickedSubmitButtonElem = clickedSubmitButton.headOption.map(_.asInstanceOf[Button])
    val clickedSubmitButtonSettings = clickedSubmitButtonElem.flatMap { e =>
      e.getAttributeOpt("data-progressive").flatMap(SubmitButtonSettings.fromJson)
    }

    val action = formElement.getAttribute("action")
    val clickedSubmitButtonFormMethod = clickedSubmitButtonElem.flatMap(_.formMethod.toOption)
    val method = clickedSubmitButtonFormMethod.getOrElse(formElement.method)
    val isGet = method.toLowerCase === "get"
    val serializedForm = form.serialize()

    val targetAction = {
      val a = clickedSubmitButtonElem.flatMap(_.formAction.toOption).orElse(formSettings.ajaxAction).getOrElse(action)
      if (isGet) queryStringService.appendQueryString(a, serializedForm) else a
    }

    val targetOpt = clickedSubmitButtonSettings.flatMap(_.target) match {
      case Some(t) => getTarget(clickedSubmitButton, Some(t))
      case None => getTarget(form, formSettings.target)
    }

    val isSecondarySubmitButton = clickedSubmitButtonFormMethod.isDefined

    if (isGet && !isSecondarySubmitButton) {
      handleGetFormSubmit(formElement, action, targetAction, targetOpt)
    }

    val isFileUpload = formElement.enctype === "multipart/form-data"

    val fut = {
      val request: AjaxRequest = {
        val headers = if (isFileUpload) Map.empty[String, String] else Map("Content-Type" -> "application/x-www-form-urlencoded")
        ajaxService.ajax(method, targetAction, Some(new FormData(formElement)), headers)
      }

      val busyMessage = clickedSubmitButtonSettings.flatMap(_.busyMessage).orElse(formSettings.busyMessage)
      val elemToRemove = if (formSettings.remove) Some(form.closest(".item")) else None
      val trigger = if (clickedSubmitButton.nonEmpty) clickedSubmitButton else form
      fadeOutFadeIn(request.future, trigger, targetOpt, formSettings.reloadPage, elemToRemove, busyMessage)
    }

    fut map { _ =>
      targetOpt.foreach(target => focusTargetIfRequired(formSettings, target))
      triggerRefreshIfRequired(formSettings, form.closest("[data-refresh]"))
      if (isFileUpload) {
        formElement.reset()
      }
    }
  }

  private def handleGetFormSubmit(formElement: Form, action: String, targetAction: String, targetOpt: Option[JQuery]): Unit = {
    updateUri(formElement, action)
    focusManagementService.dismissKeyboard(formElement)
    updateAutoRefresh(targetAction, targetOpt)
  }

  private def updateAutoRefresh(targetAction: String, targetOpt: Option[JQuery]): Unit = {
    targetOpt.foreach(target => refreshService.updateAutoRefresh(target, targetAction))
  }

  private def updateUri(formElement: Form, action: String): Unit = {
    val paramsForQueryString = queryStringService.paramsForQueryString(formElement)
    val queryString = queryStringService.toQueryString(paramsForQueryString)
    val changingPath = action =/= window.location.pathname
    val newUri = if (changingPath) {
      queryStringService.appendQueryString(action, queryString)
    } else {
      updateQueryString(paramsForQueryString)
    }
    historyService.pushState(newUri)
  }

  private def fadeOutFadeIn(
    request: Future[AjaxResponse],
    trigger: JQuery,
    targetOpt: Option[JQuery],
    reloadPage: Boolean,
    elemToRemove: Option[JQuery],
    busyMessage: Option[String]
  ): Future[Unit] = {
    val preRender = (target: JQuery) => eventHandlerSetupService.setup(target, refreshService)

    enableDisableService.disable(trigger)
    targetOpt.foreach(target => refreshService.pauseAutoRefresh(target))

    val fut = transitionsService.fadeOutFadeIn(request, targetOpt, busyMessage, reloadPage, elemToRemove, preRender)

    fut.onComplete { _ =>
      enableDisableService.enable(trigger)
      targetOpt.foreach(target => refreshService.resumeAutoRefresh(target))
    }

    fut
  }
}
