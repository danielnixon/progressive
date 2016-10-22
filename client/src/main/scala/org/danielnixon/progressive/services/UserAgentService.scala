package org.danielnixon.progressive.services

import org.scalajs.dom.Window

class UserAgentService(window: Window) {
  val isTouchDevice = window.hasOwnProperty("ontouchstart")
}