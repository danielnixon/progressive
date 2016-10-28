package org.danielnixon.progressive

import org.danielnixon.progressive.services._
import org.scalajs.dom

class Progressive {
  def initialize(
    views: Views,
    elements: KeyElements,
    eventHandlers: EventHandlers
  ): Unit = {

    val historyService = new HistoryService(dom.window)
    val ajaxService = new AjaxService
    val eventHandlerSetupService = new EventHandlerSetupService(eventHandlers.additionalSetupInitial, eventHandlers.additionalSetup)
    val refreshService = new RefreshService(Global.virtualDom, Global.vdomParser, eventHandlerSetupService, ajaxService, eventHandlers.applyDiff)
    val userAgentService = new UserAgentService(dom.window)
    val formSerializer = new FormSerializer

    val hijaxService = new HijaxService(
      dom.window,
      new QueryStringService(formSerializer, eventHandlers.includeInQueryString),
      historyService,
      userAgentService,
      new TransitionsService(dom.window, elements.announcementsElement, elements.errorElement, new AnimationService, views),
      new FocusManagementService(dom.window, eventHandlers.scrollOffset _, userAgentService),
      refreshService,
      new EnableDisableService,
      ajaxService,
      eventHandlerSetupService,
      formSerializer,
      eventHandlers.preFormSubmit,
      eventHandlers.postFormSubmit
    )

    historyService.initializeHistory()
    dom.window.onpopstate = historyService.onPopState _

    eventHandlerSetupService.setupInitial(elements.body, refreshService, hijaxService)
  }
}
