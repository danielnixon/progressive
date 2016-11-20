package org.danielnixon.progressive

import org.danielnixon.progressive.services._
import org.scalajs.dom

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
    eventHandlers: EventHandlers
  ): Unit = {

    val userAgentService = new UserAgentService(dom.window)

    if (userAgentService.meetsRequirements) {

      val historyService = new HistoryService(dom.window)
      val ajaxService = new AjaxService
      val eventHandlerSetupService = new EventHandlerSetupService(eventHandlers.additionalSetupInitial, eventHandlers.additionalSetup)
      val refreshService = new RefreshService(Global.virtualDom, Global.vdomParser, eventHandlerSetupService, ajaxService, eventHandlers.applyDiff)

      val formSerializer = new FormSerializer

      val hijaxService = new HijaxService(
        dom.window,
        new QueryStringService(formSerializer),
        historyService,
        userAgentService,
        new TransitionsService(dom.window, elements.announcementsElement, elements.errorElement, new AnimationService, views),
        new FocusManagementService(dom.window, eventHandlers.scrollOffset _, userAgentService),
        refreshService,
        new EnableDisableService,
        ajaxService,
        eventHandlerSetupService,
        formSerializer,
        new EventService,
        eventHandlers.preFormSubmit,
        eventHandlers.postFormSubmit
      )

      historyService.initializeHistory()
      dom.window.onpopstate = historyService.onPopState _

      eventHandlerSetupService.setupInitial(elements.body, refreshService, hijaxService)
    }
  }
}
