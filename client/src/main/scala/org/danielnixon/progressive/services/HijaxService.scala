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
    preFormSubmit: Element => Boolean,
    postFormSubmit: Element => Unit
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
      refreshService.refresh(refreshTarget(0), userTriggered = true)
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
              if (preFormSubmit(form(0))) {
                val fut = submitAjaxForm(form, formSettings, clickedSubmitButton)
                fut.onComplete(_ => postFormSubmit(form(0)))
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
    val submitButton = clickedSubmitButton.headOption.map(_.asInstanceOf[Button])
    val submitButtonSettings = submitButton.flatMap { e =>
      e.getAttributeOpt("data-progressive").flatMap(SubmitButtonSettings.fromJson)
    }

    val settings = formSettings.copy(
      ajaxAction = submitButton.flatMap(_.getAttributeOpt("formaction")).orElse(formSettings.ajaxAction),
      busyMessage = submitButtonSettings.flatMap(_.busyMessage).orElse(formSettings.busyMessage)
    )

    val action = formElement.getAttribute("action")
    val clickedSubmitButtonFormMethod = submitButton.flatMap(_.formMethod.toOption)
    val method = clickedSubmitButtonFormMethod.getOrElse(formElement.method)
    val isGet = method.toLowerCase === "get"
    val serializedForm = form.serialize()

    val targetAction = {
      val a = settings.ajaxAction.getOrElse(action)
      if (isGet) queryStringService.appendQueryString(a, serializedForm) else a
    }

    val targetOpt = submitButtonSettings.flatMap(_.target) match {
      case Some(t) => getTarget(clickedSubmitButton, Some(t))
      case None => getTarget(form, settings.target)
    }

    val isSecondarySubmitButton = clickedSubmitButtonFormMethod.isDefined

    if (isGet && !isSecondarySubmitButton) {
      handleGetFormSubmit(formElement, action, targetAction, targetOpt)
    }

    val isFileUpload = formElement.enctype === "multipart/form-data"
    val request = makeRequest(formElement, method, serializedForm, targetAction, isFileUpload).future

    val trigger = if (clickedSubmitButton.nonEmpty) clickedSubmitButton else form
    val elemToRemove = if (formSettings.remove) Some(trigger.closest(".item")) else None
    val fut = fadeOutFadeIn(request, trigger, targetOpt, formSettings.reloadPage, elemToRemove, formSettings.busyMessage)

    fut map { _ =>
      targetOpt.foreach(target => focusTargetIfRequired(settings, target))
      triggerRefreshIfRequired(settings, form.closest("[data-refresh]"))
      if (isFileUpload) {
        formElement.reset()
      }
    }
  }

  private def makeRequest(formElement: Form, method: String, serializedForm: String, targetAction: String, isFileUpload: Boolean): AjaxRequest = {
    val headers = if (isFileUpload) Map.empty[String, String] else Map("Content-Type" -> "application/x-www-form-urlencoded")
    ajaxService.ajax(method, targetAction, Some(if (isFileUpload) new FormData(formElement) else serializedForm), headers)
  }

  private def handleGetFormSubmit(formElement: Form, action: String, targetAction: String, targetOpt: Option[JQuery]): Unit = {
    updateUri(formElement, action)
    focusManagementService.dismissKeyboard(formElement)
    updateAutoRefresh(targetAction, targetOpt)
  }

  private def updateAutoRefresh(targetAction: String, targetOpt: Option[JQuery]): Unit = {
    targetOpt.foreach(target => refreshService.updateRefresh(target(0), targetAction))
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
    val preRender = (target: JQuery) => eventHandlerSetupService.setup(target(0), refreshService)

    enableDisableService.disable(trigger)
    targetOpt.foreach(target => refreshService.pauseAutoRefresh(target(0)))

    val fut = transitionsService.fadeOutFadeIn(request, targetOpt, busyMessage, reloadPage, elemToRemove, preRender)

    fut.onComplete { _ =>
      enableDisableService.enable(trigger)
      targetOpt.foreach(target => refreshService.resumeAutoRefresh(target(0)))
    }

    fut
  }
}
