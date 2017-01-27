package org.danielnixon.progressive.services

import org.danielnixon.progressive.shared.Wart
import org.danielnixon.saferdom.Window
import org.danielnixon.saferdom.implicits.lib._
import org.danielnixon.saferdom.implicits.html._

import scala.scalajs.js

class UserAgentService(window: Window) {

  val isTouchDevice: Boolean = window.document.documentElement.exists(d => isDefined(d, "ontouchstart"))

  /**
    * True if the current user agent meets Progressive's requirements, false otherwise.
    */
  val meetsRequirements: Boolean = {
    isDefined(window, "WeakMap") &&
      isDefined(window, "history") &&
      isDefined(window.history, "pushState") &&
      isDefined(window.document, "querySelector") &&
      window.document.body.exists(b => isDefined(b, "matches"))
  }

  @SuppressWarnings(Array(Wart.AsInstanceOf))
  private def isDefined(obj: js.Object, prop: String): Boolean = {
    !js.isUndefined(obj.asInstanceOf[js.Dynamic].selectDynamic(prop))
  }
}