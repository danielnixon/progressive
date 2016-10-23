package org.danielnixon.progressive.services

import org.danielnixon.progressive.Views
import org.danielnixon.progressive.extensions.jquery.JQuerySeq
import org.querki.jquery._
import org.scalajs.dom.Window
import org.danielnixon.progressive.shared.Wart
import org.danielnixon.progressive.shared.api.AjaxResponse

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

class TransitionsService(
    window: Window,
    announcementsElement: JQuery,
    errorElement: JQuery,
    animationService: AnimationService,
    views: Views
) {

  private def announce(message: String): Unit = {
    announcementsElement.text(message)
  }

  private def fadeOutAnimation(target: JQuery, busyMessage: Option[String]): Future[Unit] = {
    val skipFadeOut = target.text.trim.isEmpty
    val promise = animationService.transitionOut(target.headOption, if (skipFadeOut) Some(0) else None)

    busyMessage.foreach(announce)

    promise.flatMap { _ =>
      busyMessage map { m =>
        target.html(views.loading(m).render)
        animationService.transitionIn(target.headOption, preserveHeight = true)
      } getOrElse {
        Future.successful(())
      }
    }
  }

  private def fadeIn(target: JQuery, result: String, preRender: JQuery => Unit): Future[Unit] = {
    animationService.transitionOut(target.headOption, None).flatMap { _ =>
      target.html(result)
      preRender(target)
      animationService.transitionIn(target.headOption)
    }
  }

  private def displayError(targetOpt: Option[JQuery], e: AjaxRequestException, preRender: JQuery => Unit) = {
    announce(e.message)

    targetOpt map { target =>
      fadeIn(target, views.error(e.html).render, preRender)
    } getOrElse {
      fadeIn(errorElement, views.globalError(e.html).render, preRender)
    }
  }

  private def handleResponse(
    response: AjaxResponse,
    targetOpt: Option[JQuery],
    reloadPage: Boolean,
    elemToRemove: Option[JQuery],
    preRender: JQuery => Unit
  ): Future[Unit] = {
    if (reloadPage) {
      window.location.reload(true)
      Future.successful(())
    } else {
      response.message.foreach(announce)

      elemToRemove match {
        case Some(elem) =>
          val fut = animationService.transitionOut(elem.headOption, None)
          fut.onComplete(_ => elem.remove())
          fut
        case None =>
          targetOpt map { target =>
            fadeIn(target, response.html, preRender)
          } getOrElse {
            Future.successful(())
          }
      }
    }
  }

  @SuppressWarnings(Array(Wart.Any))
  def fadeOutFadeIn(
    request: Future[AjaxResponse],
    targetOpt: Option[JQuery],
    busyMessage: Option[String],
    reloadPage: Boolean,
    elemToRemove: Option[JQuery],
    preRender: JQuery => Unit
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

    fut.onFailure {
      case e: AjaxRequestException => displayError(targetOpt, e, preRender)
    }

    fut
  }
}
