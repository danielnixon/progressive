package org.danielnixon.progressive.services

import org.danielnixon.progressive.shared.Wart
import org.scalajs.dom.Window
import org.danielnixon.saferdom.implicits._

import scala.scalajs.js

class UserAgentService(window: Window) {

  val isTouchDevice: Boolean = window.document.documentElementOpt.exists(d => isDefined(d, "ontouchstart"))

  /**
    * True if the current user agent meets Progressive's requirements, false otherwise.
    */
  val meetsRequirements: Boolean = {
    isDefined(window, "WeakMap") &&
      isDefined(window, "history") &&
      isDefined(window.history, "pushState") &&
      isDefined(window.document, "querySelector") &&
      window.document.bodyOpt.exists(b => isDefined(b, "matches")) &&
      window.document.bodyOpt.exists(b => isDefined(b, "closest"))
  }

  @SuppressWarnings(Array(Wart.AsInstanceOf))
  private def isDefined(obj: js.Object, prop: String): Boolean = {
    !js.isUndefined(obj.asInstanceOf[js.Dynamic].selectDynamic(prop))
  }
}