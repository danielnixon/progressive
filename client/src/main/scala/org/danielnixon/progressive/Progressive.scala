package org.danielnixon.progressive

import org.danielnixon.progressive.shared.Wart
import org.danielnixon.saferdom

import scala.scalajs.js

/**
  * Entry point for Progressive.
  */
class Progressive {
  /**
    * Initialize Progressive. Call this once per page load.
    * @param components The [[Components]] implementation.
    */
  def initialize(components: Components): Unit = {
    if (components.userAgentService.meetsRequirements && dependenciesExist) {
      components.historyService.initializeHistory()
      saferdom.window.onpopstate = components.historyService.onPopState _
      components.eventHandlerSetupService.setupInitial(components.elements.body)
    }
  }

  @SuppressWarnings(Array(Wart.Any))
  private val dependenciesExist: Boolean = !Seq(Global.virtualDom, Global.vdomParser).exists(js.isUndefined)
}
