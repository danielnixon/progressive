package org.danielnixon.progressive.services

import org.danielnixon.progressive.extensions.dom.ElementWrapper
import org.scalajs.dom.html

import scala.concurrent.{ Future, Promise }
import scala.scalajs.js.timers._
import scala.util.Success

class AnimationService {
  private def transition(elementOpt: Option[html.Element], show: Boolean, preserveHeight: Boolean, durationOpt: Option[Int]): Future[Unit] = {
    elementOpt map { htmlElement =>

      val minHeight = htmlElement.getAttributeOpt("data-initial-min-height") getOrElse {
        val m = htmlElement.style.minHeight
        htmlElement.setAttribute("data-initial-min-height", m)
        m
      }

      val newMinHeight = if (preserveHeight) htmlElement.getBoundingClientRect.height + "px" else minHeight

      val duration = {
        val defaultDuration = 400
        durationOpt.getOrElse(defaultDuration)
      }

      htmlElement.style.minHeight = newMinHeight
      htmlElement.style.transition = s"opacity ${duration}ms ease"
      htmlElement.style.opacity = if (show) "1" else "0"

      val promise = Promise[Unit]()
      setTimeout(duration.toDouble)(promise.complete(Success(())))
      promise.future
    } getOrElse Future.successful(())
  }

  def transitionOut(elementOpt: Option[html.Element], duration: Option[Int]): Future[Unit] = {
    transition(elementOpt, false, true, duration)
  }

  def transitionIn(elementOpt: Option[html.Element], preserveHeight: Boolean = false): Future[Unit] = {
    transition(elementOpt, true, preserveHeight, None)
  }
}
