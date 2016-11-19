package org.danielnixon.progressive.services

import org.scalajs.dom._
import org.danielnixon.progressive.extensions.core.StringWrapper
import org.danielnixon.progressive.extensions.dom._
import org.danielnixon.progressive.shared.Wart
import org.danielnixon.progressive.shared.api._
import org.danielnixon.progressive.shared.http.{ HeaderNames, MimeTypes }
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
    formSerializer: FormSerializer,
    eventService: EventService,
    preFormSubmit: Form => Boolean,
    postFormSubmit: Form => Unit
) {

  private def updateQueryString(params: Seq[QueryStringParam]): String = {
    val path = window.location.pathname
    val search = window.location.search
    queryStringService.updateQueryString(path, search, params)
  }

  @SuppressWarnings(Array(Wart.AsInstanceOf))
  private def getTargetElement(element: Element, target: Target): Option[html.Element] = {
    val targetElement = target match {
      case Target.Next => element.nextElementSiblingOpt
      case Target.Parent => element.parentOpt
      case Target.ChildTarget => element.querySelectorOpt(s".${CssClasses.target}")
      case Target.ClosestRefresh => element.closest(s"[${DataAttributes.refresh}]")
    }

    targetElement.map(_.asInstanceOf[html.Element])
  }

  private def triggerRefreshIfRequired(settings: FormSettings, refreshTarget: Element): Unit = {
    if (settings.triggerRefresh) {
      refreshService.refresh(refreshTarget, userTriggered = true)
    }
  }

  @SuppressWarnings(Array(Wart.AsInstanceOf))
  def ajaxLinkClick(e: MouseEvent, element: Anchor): Unit = {

    if (eventService.shouldHijackLinkClick(e)) {
      element.getAttributeOpt(DataAttributes.progressive).flatMap(LinkSettings.fromJson) foreach { settings =>

        val targetOpt = getTargetElement(element, settings.target)
        val queryStringArray = queryStringService.extractQueryStringParams(element.href)
        val newUri = updateQueryString(queryStringArray)
        historyService.pushState(newUri)
        val ajaxHref = settings.href.getOrElse(element.href)
        val request = ajaxService.get(ajaxHref)

        targetOpt foreach { target =>
          refreshService.updateRefresh(target, ajaxHref)
          if (settings.focusTarget) {
            focusManagementService.setFocus(target)
          }
        }

        fadeOutFadeIn(request.future, element, targetOpt, false, None, settings.busyMessage)
      }

      e.preventDefault()
    }
  }

  private def clearClickedButtons(form: Element) = {
    val submitButtons = form.querySelectorAll("button[type=submit]").map(_.asInstanceOf[Button])
    submitButtons.foreach(_.removeAttribute("data-clicked"))
  }

  @SuppressWarnings(Array(Wart.AsInstanceOf))
  def ajaxSubmitButtonClick(button: Button): Unit = {
    button.closest("form").foreach(clearClickedButtons)
    button.setAttribute("data-clicked", "true")
  }

  @SuppressWarnings(Array(Wart.AsInstanceOf))
  def ajaxFormSubmit(e: Event, form: Form): Unit = {

    val result = form.getAttributeOpt(DataAttributes.progressive).flatMap(FormSettings.fromJson) match {
      case None => false
      case Some(formSettings) =>

        val submitButton = form.querySelectorOpt("button[type=submit][data-clicked]").map(_.asInstanceOf[Button])
        clearClickedButtons(form)
        val confirmed = formSettings.confirmMessage.forall(window.confirm)

        if (!confirmed) {
          false
        } else {
          formSettings.confirmedAction.foreach(action => form.setAttribute("action", action))

          if (!formSettings.ajax) {
            true
          } else {
            if (preFormSubmit(form)) {
              val fut = submitAjaxForm(form, formSettings, submitButton)
              fut.onComplete(_ => postFormSubmit(form))
            }
            false
          }
        }
    }

    if (!result) {
      e.preventDefault()
    }
  }

  @SuppressWarnings(Array(Wart.AsInstanceOf))
  private def submitAjaxForm(form: Form, formSettings: FormSettings, submitButton: Option[html.Button]): Future[Unit] = {

    val settings = mergeSettings(formSettings, submitButton)

    val action = form.getAttribute("action")
    val clickedSubmitButtonFormMethod = submitButton.flatMap(_.formMethod.toOption)
    val method = clickedSubmitButtonFormMethod.getOrElse(form.method)
    val isGet = method.toLowerCase === "get"

    val serializedForm = formSerializer.serialize(form)

    val targetAction = {
      val a = settings.ajaxAction.getOrElse(action)
      if (isGet) queryStringService.appendQueryString(a, serializedForm) else a
    }

    val targetOpt = submitButton match {
      case Some(b) => settings.target.flatMap(x => getTargetElement(b, x))
      case None => settings.target.flatMap(x => getTargetElement(form, x))
    }

    val isSecondarySubmitButton = clickedSubmitButtonFormMethod.isDefined

    if (isGet && !isSecondarySubmitButton) {
      handleGetFormSubmit(form, action, targetAction, targetOpt)
    }

    val isFileUpload = form.enctype === MimeTypes.FORM_DATA
    val request = makeRequest(form, method, serializedForm, targetAction, isFileUpload).future

    val trigger = submitButton.getOrElse(form)
    val elemToRemove = if (settings.remove) trigger.closest(s".${CssClasses.removable}").map(_.asInstanceOf[html.Element]) else None
    val closestRefresh = form.closest(s"[${DataAttributes.refresh}]")
    val elemToRemoveClosestRefresh = elemToRemove.flatMap(_.closest(s"[${DataAttributes.refresh}]"))
    elemToRemoveClosestRefresh.foreach(refreshService.invalidate)

    val fut = fadeOutFadeIn(request, trigger, targetOpt, settings.reloadPage, elemToRemove, settings.busyMessage)

    if (settings.focusTarget) {
      targetOpt.foreach(focusManagementService.setFocus)
    }

    fut map { _ =>
      closestRefresh.foreach(x => triggerRefreshIfRequired(settings, x))
      if (isFileUpload) {
        form.reset()
      }
    }
  }

  private def mergeSettings(formSettings: FormSettings, submitButton: Option[Button]): FormSettings = {

    val submitButtonSettings = submitButton.flatMap { e =>
      e.getAttributeOpt(DataAttributes.progressive).flatMap(SubmitButtonSettings.fromJson)
    }

    formSettings.copy(
      ajaxAction = submitButton.flatMap(_.getAttributeOpt("formaction")).orElse(formSettings.ajaxAction),
      busyMessage = submitButtonSettings.flatMap(_.busyMessage).orElse(formSettings.busyMessage),
      target = submitButtonSettings.flatMap(_.target).orElse(formSettings.target)
    )
  }

  private def makeRequest(formElement: Form, method: String, serializedForm: String, targetAction: String, isFileUpload: Boolean): AjaxRequest = {
    val headers = if (isFileUpload) Map.empty[String, String] else Map(HeaderNames.CONTENT_TYPE -> MimeTypes.FORM)
    ajaxService.ajax(method, targetAction, Some(if (isFileUpload) new FormData(formElement) else serializedForm), headers)
  }

  private def handleGetFormSubmit(
    form: Form,
    action: String,
    targetAction: String,
    targetOpt: Option[Element]
  ): Unit = {
    updateUri(form, action)
    focusManagementService.dismissKeyboard(form)
    targetOpt.foreach(target => refreshService.updateRefresh(target, targetAction))
  }

  private def updateUri(form: Form, action: String): Unit = {
    val paramsForQueryString = queryStringService.paramsForQueryString(form)
    val changingPath = action =/= window.location.pathname

    val newUri = if (changingPath) {
      val queryString = queryStringService.toQueryString(paramsForQueryString)
      queryStringService.appendQueryString(action, queryString)
    } else {
      updateQueryString(paramsForQueryString)
    }
    historyService.pushState(newUri)
  }

  private def fadeOutFadeIn(
    request: Future[AjaxResponse],
    trigger: html.Element,
    targetOpt: Option[html.Element],
    reloadPage: Boolean,
    elemToRemove: Option[html.Element],
    busyMessage: Option[String]
  ): Future[Unit] = {
    val preRender = (target: Element) => eventHandlerSetupService.setup(target, refreshService)

    enableDisableService.disable(trigger)
    targetOpt.foreach(refreshService.pauseAutoRefresh)

    val fut = transitionsService.fadeOutFadeIn(request, targetOpt, busyMessage, reloadPage, elemToRemove, preRender)

    fut.onComplete { _ =>
      enableDisableService.enable(trigger)
      targetOpt.foreach(refreshService.resumeAutoRefresh)
    }

    fut
  }
}
