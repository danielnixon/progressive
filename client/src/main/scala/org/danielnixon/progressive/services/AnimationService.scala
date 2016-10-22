package org.danielnixon.progressive.services

import org.scalajs.dom.html

import scala.concurrent.{ Future, Promise }
import scala.scalajs.js.timers._
import scala.util.Success

class AnimationService {
  // scalastyle:off magic.number
  private def transition(elementOpt: Option[html.Element], show: Boolean, preserveHeight: Boolean, durationOpt: Option[Int]): Future[Unit] = {
    elementOpt map { htmlElement =>

      val minHeight = if (htmlElement.hasAttribute("data-initial-min-height")) {
        htmlElement.getAttribute("data-initial-min-height")
      } else {
        val m = htmlElement.style.minHeight
        htmlElement.setAttribute("data-initial-min-height", m)
        m
      }

      val newMinHeight = if (preserveHeight) htmlElement.clientHeight.toString + "px" else minHeight
      val duration = durationOpt.getOrElse(400)

      htmlElement.style.minHeight = newMinHeight
      htmlElement.style.transition = "opacity " + duration.toString + "ms ease"
      htmlElement.style.opacity = if (show) "1" else "0"

      val promise = Promise[Unit]()
      setTimeout(duration.toDouble)(promise.complete(Success(())))
      promise.future
    } getOrElse Future.successful(())
  }

  def transitionOut(elementOpt: Option[html.Element], duration: Option[Int]): Future[Unit] = {
    transition(elementOpt, false, true, duration)
  }
  // scalastyle:on magic.number

  def transitionIn(elementOpt: Option[html.Element], preserveHeight: Boolean = false): Future[Unit] = {
    transition(elementOpt, true, preserveHeight, None)
  }
}
