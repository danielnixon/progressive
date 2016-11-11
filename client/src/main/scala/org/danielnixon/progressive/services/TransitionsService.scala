package org.danielnixon.progressive.services

import org.danielnixon.progressive.Views
import org.danielnixon.progressive.extensions.dom.ElementWrapper
import org.danielnixon.progressive.shared.Wart
import org.danielnixon.progressive.shared.api.AjaxResponse
import org.scalajs.dom.{ Element, Window }
import org.scalajs.dom.raw.HTMLElement

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{ Failure, Success }

class TransitionsService(
    window: Window,
    announcementsElement: Element,
    errorElement: HTMLElement,
    animationService: AnimationService,
    views: Views
) {

  private def announce(message: String): Unit = {
    announcementsElement.textContent = message
  }

  private def fadeOutAnimation(target: HTMLElement, busyMessage: Option[String]): Future[Unit] = {
    val skipFadeOut = target.textContent.trim.isEmpty
    val transitionOutFut = animationService.transitionOut(target, if (skipFadeOut) Some(0) else None)

    busyMessage.foreach(announce)

    transitionOutFut.flatMap { _ =>
      busyMessage map { m =>
        target.innerHTML = views.loading(m).render
        animationService.transitionIn(target, preserveHeight = true)
      } getOrElse {
        Future.successful(())
      }
    }
  }

  private def fadeIn(target: HTMLElement, newHtml: String, preRender: Element => Unit): Future[Unit] = {
    animationService.transitionOut(target, None).flatMap { _ =>
      target.innerHTML = newHtml
      preRender(target)
      animationService.transitionIn(target)
    }
  }

  private def displayError(targetOpt: Option[HTMLElement], message: String, html: String, preRender: Element => Unit) = {
    announce(message)

    targetOpt map { target =>
      fadeIn(target, views.error(html).render, preRender)
    } getOrElse {
      fadeIn(errorElement, views.globalError(html).render, preRender)
    }
  }

  private def handleResponse(
    response: AjaxResponse,
    targetOpt: Option[HTMLElement],
    reloadPage: Boolean,
    elemToRemove: Option[HTMLElement],
    preRender: Element => Unit
  ): Future[Unit] = {
    if (reloadPage) {
      window.location.reload(true)
      Future.successful(())
    } else {
      response.message.foreach(announce)

      elemToRemove match {
        case Some(elem) =>
          val fut = animationService.transitionOut(elem, None)
          fut.onComplete(_ => elem.parentOpt.foreach(_.removeChild(elem)))
          fut
        case None =>
          (targetOpt, response.html) match {
            case (Some(target), Some(html)) => fadeIn(target, html, preRender)
            case _ => Future.successful(())
          }
      }
    }
  }

  @SuppressWarnings(Array(Wart.Any))
  def fadeOutFadeIn(
    request: Future[AjaxResponse],
    targetOpt: Option[HTMLElement],
    busyMessage: Option[String],
    reloadPage: Boolean,
    elemToRemove: Option[HTMLElement],
    preRender: Element => Unit
  ): Future[Unit] = {

    val animation = targetOpt.map { target =>
      fadeOutAnimation(target, busyMessage)
    } getOrElse {
      Future.successful(())
    }

    val fut = for {
      response <- request
      _ <- animation
      _ <- handleResponse(response, targetOpt, reloadPage, elemToRemove, preRender)
    } yield ()

    fut onComplete {
      case Success(_) => ()
      case Failure(e: AjaxRequestException) => displayError(targetOpt, e.message, e.html, preRender)
      case Failure(e: Throwable) => displayError(targetOpt, e.getMessage, e.getMessage, preRender)
    }

    fut
  }
}
