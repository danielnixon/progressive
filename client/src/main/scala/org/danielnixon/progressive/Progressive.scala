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
    * @param views The [[Views]] implementation.
    * @param elements The [[KeyElements]] implementation.
    * @param eventHandlers The [[EventHandlers]] implementation.
    */
  def initialize(
    views: Views,
    elements: KeyElements,
    eventHandlers: Services => EventHandlers = defaultEventHandlers
  ): Unit = {

    lazy val events: EventHandlers = eventHandlers(services)
    lazy val services: Services = new DefaultServices(views, elements, events)

    if (services.userAgentService.meetsRequirements && dependenciesExist) {
      services.historyService.initializeHistory()
      saferdom.window.onpopstate = services.historyService.onPopState _
      services.eventHandlerSetupService.setupInitial(elements.body)
    }
  }

  @SuppressWarnings(Array(Wart.Any))
  private val dependenciesExist: Boolean = !Seq(Global.virtualDom, Global.vdomParser).exists(js.isUndefined)

  private def defaultEventHandlers(services: Services) = new EventHandlers {}
}
