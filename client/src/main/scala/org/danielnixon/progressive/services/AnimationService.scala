package org.danielnixon.progressive.services

import org.danielnixon.progressive.extensions.dom.ElementWrapper
import org.scalajs.dom.html

import scala.concurrent.{ Future, Promise }
import scala.scalajs.js.timers._
import scala.util.Success

class AnimationService {
  private def transition(element: html.Element, show: Boolean, preserveHeight: Boolean, durationOpt: Option[Int]): Future[Unit] = {
    val initialMinHeight = element.getAttributeOpt("data-initial-min-height") getOrElse {
      val m = element.style.minHeight
      element.setAttribute("data-initial-min-height", m)
      m
    }

    val minHeight = if (preserveHeight) s"${element.getBoundingClientRect.height}px" else initialMinHeight

    val duration = {
      val defaultDuration = 400
      durationOpt.getOrElse(defaultDuration)
    }

    element.style.minHeight = minHeight
    element.style.transition = s"opacity ${duration}ms ease"
    element.style.opacity = if (show) "1" else "0"

    val promise = Promise[Unit]()
    setTimeout(duration.toDouble)(promise.complete(Success(())))
    promise.future
  }

  def transitionOut(element: html.Element, duration: Option[Int]): Future[Unit] = {
    transition(element, false, true, duration)
  }

  def transitionIn(element: html.Element, preserveHeight: Boolean = false): Future[Unit] = {
    transition(element, true, preserveHeight, None)
  }
}
