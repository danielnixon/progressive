package org.danielnixon.progressive.services

import org.scalajs.dom._
import org.danielnixon.saferdom.implicits.{ html => _, _ }
import org.danielnixon.progressive.extensions.core.StringWrapper
import org.danielnixon.progressive.extensions.dom._
import org.danielnixon.progressive.shared.api._
import org.danielnixon.progressive.shared.http.{ HeaderNames, MimeTypes }
import org.scalajs.dom.html.{ Anchor, Button, Form }
import org.scalajs.dom.raw.Element

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
    targetService: TargetService,
    preFormSubmit: (Form, Option[html.Element]) => Boolean,
    postFormSubmit: (Form, Option[html.Element]) => Unit
) {

  private def updateQueryString(params: Seq[QueryStringParam]): String = {
    val path = window.location.pathname
    val search = window.location.search
    queryStringService.updateQueryString(path, search, params)
  }

  def ajaxLinkClick(e: MouseEvent, element: Anchor): Unit = {

    if (eventService.shouldHijackLinkClick(e)) {
      element.getAttributeOpt(DataAttributes.progressive).flatMap(LinkSettings.fromJson) foreach { settings =>

        val targetOpt = targetService.getTargetElement(element, settings.target)
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

        fadeOutFadeIn(request.future, element, targetOpt, false, None, settings.busyMessage, form = None)
      }

      e.preventDefault()
    }
  }

  private def clearClickedButtons(form: Element) = {
    val submitButtons = form.querySelectorAll("button[type=submit]").collect({ case b: Button => b })
    submitButtons.foreach(_.removeAttribute("data-clicked"))
  }

  def ajaxSubmitButtonClick(button: Button): Unit = {
    button.closest("form").foreach(clearClickedButtons)
    button.setAttribute("data-clicked", "true")
  }

  def ajaxFormSubmit(e: Event, form: Form): Unit = {

    val result = form.getAttributeOpt(DataAttributes.progressive).flatMap(FormSettings.fromJson) match {
      case None => false
      case Some(formSettings) =>

        val submitButton = form.querySelectorOpt("button[type=submit][data-clicked]").collect({ case b: Button => b })
        clearClickedButtons(form)
        val confirmed = formSettings.confirmMessage.forall(window.confirm)

        if (!confirmed) {
          false
        } else {
          formSettings.confirmedAction.foreach(action => form.setAttribute("action", action))

          if (!formSettings.ajax) {
            true
          } else {
            if (preFormSubmit(form, submitButton)) {
              val fut = submitAjaxForm(form, formSettings, submitButton)
              fut.onComplete(_ => postFormSubmit(form, submitButton))
            }
            false
          }
        }
    }

    if (!result) {
      e.preventDefault()
    }
  }

  private def submitAjaxForm(form: Form, formSettings: FormSettings, submitButton: Option[html.Button]): Future[Unit] = {

    val settings = mergeSettings(formSettings, submitButton)

    val action = form.getAttributeOpt("action").getOrElse("")
    val clickedSubmitButtonFormMethod = submitButton.flatMap(_.formMethod.toOption)
    val method = clickedSubmitButtonFormMethod.getOrElse(form.method)
    val isGet = method.toLowerCase === "get"

    val serializedForm = formSerializer.serialize(form)

    val targetAction = {
      val a = settings.ajaxAction.getOrElse(action)
      if (isGet) queryStringService.appendQueryString(a, serializedForm) else a
    }

    val trigger = submitButton.getOrElse(form)
    val getTarget = (targetService.getTargetElement _).curried(trigger)
    val targetOpt = settings.target.flatMap(getTarget)
    val refreshTargetOpt = settings.refreshTarget.flatMap(getTarget)

    val isSecondarySubmitButton = clickedSubmitButtonFormMethod.isDefined

    if (isGet && !isSecondarySubmitButton) {
      handleGetFormSubmit(form, action, targetAction, targetOpt)
    }

    val isFileUpload = form.enctype === MimeTypes.FORM_DATA
    val request = makeRequest(form, method, serializedForm, targetAction, isFileUpload).future

    val elemToRemove = if (settings.remove) {
      trigger.closest(s".${CssClasses.removable}").collect({ case e: html.Element => e })
    } else {
      None
    }
    val elemToRemoveClosestRefresh = elemToRemove.flatMap(_.closest(s"[${DataAttributes.refresh}]"))
    elemToRemoveClosestRefresh.foreach(refreshService.invalidate)

    val fut = fadeOutFadeIn(request, trigger, targetOpt, settings.reloadPage, elemToRemove, settings.busyMessage, Some(form))

    if (settings.focusTarget) {
      targetOpt.foreach(focusManagementService.setFocus)
    }

    fut map { _ =>
      refreshTargetOpt.foreach(x => refreshService.refresh(x, userTriggered = true))
      if (settings.resetForm) {
        form.reset()
      }
    }
  }

  private def mergeSettings(formSettings: FormSettings, submitButton: Option[Element]): FormSettings = {

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
    busyMessage: Option[String],
    form: Option[html.Form]
  ): Future[Unit] = {
    val preRender = (target: Element) => eventHandlerSetupService.setup(target)

    enableDisableService.disable(trigger)
    targetOpt.foreach(refreshService.pauseAutoRefresh)

    val fut = transitionsService.fadeOutFadeIn(request, targetOpt, busyMessage, reloadPage, elemToRemove, preRender, form)

    fut.onComplete { _ =>
      enableDisableService.enable(trigger)
      targetOpt.foreach(refreshService.resumeAutoRefresh)
    }

    fut
  }
}
