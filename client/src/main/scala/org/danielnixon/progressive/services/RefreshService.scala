package org.danielnixon.progressive.services

import org.danielnixon.progressive.extensions.dom.ElementWrapper
import org.danielnixon.progressive.extensions.jquery.{ JQuerySeq, JQueryWrapper }
import org.danielnixon.progressive.extensions.virtualdom.PatchObjectWrapper
import org.danielnixon.progressive.facades.virtualdom.{ VDomParser, VTree, VirtualDom }
import org.danielnixon.progressive.shared.api.RefreshSettings
import org.querki.jquery._
import org.scalajs.dom.raw.HTMLElement

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js.timers.setInterval

class RefreshService(
    virtualDom: VirtualDom,
    vdomParser: VDomParser,
    eventHandlerSetupService: EventHandlerSetupService,
    ajaxService: AjaxService,
    applyDiff: JQuery => Boolean
) {

  private def vDomTarget(element: JQuery): HTMLElement = {
    val refreshContent = element.find(".refresh-content")
    refreshContent.headOption.getOrElse(element.head)
  }

  private def createVdom(element: JQuery): VTree = {
    val target = vDomTarget(element)
    vdomParser(target)
  }

  def refresh(element: JQuery, userTriggered: Boolean): Future[Unit] = {
    val alreadyRefreshing = element.data("refresh-request").isDefined

    element.dataT[String]("refresh") match {
      case Some(url) if !alreadyRefreshing =>
        val request = ajaxService.get(url)
        element.data("refresh-request", request)
        val fut = request.future.map { ajaxResponse =>
          val shouldApplyDiff = userTriggered || applyDiff(element)

          if (shouldApplyDiff) {
            // Get existing virtual DOM.
            val targetVdom = element.dataT[VTree]("vdom").getOrElse(createVdom(element))

            // Create new virtual DOM.
            val newVdom = vdomParser(ajaxResponse.html)
            element.data("vdom", newVdom)

            // Calculate patch.
            val patchObject = virtualDom.diff(targetVdom, newVdom)

            if (!patchObject.isEmpty) {
              // Apply patch.
              val target = vDomTarget(element)
              virtualDom.patch(target, patchObject)

              // Re-bind event handlers, etc.
              eventHandlerSetupService.setup(element, this)
            }
          }
        }

        fut.onComplete { _ => element.removeData("refresh-request") }

        fut

      case _ => Future.successful(())
    }
  }

  def setupRefresh(element: JQuery): Unit = {
    val div = element(0)

    div.getAttributeOpt("data-refresh").flatMap(RefreshSettings.fromJson) foreach { settings =>

      element.data("vdom", createVdom(element))
      element.data("refresh", settings.url)

      settings.interval map { interval =>
        setInterval(interval.toDouble) {
          val paused = element.dataT[Boolean]("paused").getOrElse(false)
          if (!paused) {
            refresh(element, userTriggered = false)
          }
        }
      }
    }
  }

  def updateAutoRefresh(element: JQuery, url: String): Unit = {
    if (element.is("[data-refresh]")) {
      element.data("refresh", url)
      element.removeData("vdom")
    }
  }

  def pauseAutoRefresh(element: JQuery): Unit = {
    // Cancel existing request.
    val request = element.dataT[AjaxRequest]("refresh-request")
    element.removeData("refresh-request")
    request.foreach(_.abort())

    element.data("paused", true)
  }

  def resumeAutoRefresh(element: JQuery): Unit = {
    element.data("paused", false)
  }
}
