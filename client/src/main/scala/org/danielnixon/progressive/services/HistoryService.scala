package org.danielnixon.progressive.services

import org.scalajs.dom.{ PopStateEvent, Window }
import org.danielnixon.progressive.shared.Wart

import scala.scalajs.js.{ Dynamic, isUndefined }

class HistoryService(window: Window) {

  def pushState(location: String): Unit = {
    window.history.pushState(Dynamic.literal(location = location), "", location)
  }

  def initializeHistory(): Unit = {
    val location = window.location.href
    window.history.replaceState(Dynamic.literal(location = location), "", location)
  }

  @SuppressWarnings(Array(Wart.AsInstanceOf))
  def onPopState(e: PopStateEvent): Unit = {
    val state = e.state.asInstanceOf[Dynamic]

    if (!isUndefined(state) && !isUndefined(state.location)) {
      window.location.assign(state.location.asInstanceOf[String])
    }
  }
}
