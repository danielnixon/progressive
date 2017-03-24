package org.danielnixon.progressive

import org.danielnixon.progressive.services._
import org.danielnixon.saferdom

trait Components {
  this: EventHandlers =>

  def views: Views

  def elements: KeyElements

  lazy val userAgentService: UserAgentService = new UserAgentService(saferdom.window)

  lazy val historyService = new HistoryService(saferdom.window)

  lazy val formSerializer = new FormSerializer

  lazy val eventService: EventService = new EventService

  lazy val focusManagementService: FocusManagementService =
    new FocusManagementService(saferdom.window, scrollOffset _, userAgentService)

  lazy val ajaxService: AjaxService = new AjaxService

  lazy val targetService: TargetService = new TargetService

  lazy val refreshService: RefreshService =
    new RefreshService(Global.virtualDom, Global.vdomParser, eventHandlerSetupService, ajaxService, applyDiff)

  lazy val hijaxService: HijaxService = {
    new HijaxService(
      saferdom.window,
      new QueryStringService(formSerializer),
      historyService,
      userAgentService,
      new TransitionsService(
        saferdom.window,
        elements.announcementsElement,
        elements.errorElement,
        new AnimationService,
        views,
        new VDomService(Global.virtualDom, Global.vdomParser)
      ),
      focusManagementService,
      refreshService,
      new EnableDisableService,
      ajaxService,
      eventHandlerSetupService,
      formSerializer,
      eventService,
      targetService,
      preFormSubmit,
      postFormSubmit
    )
  }

  lazy val eventHandlerSetupService: EventHandlerSetupService =
    new EventHandlerSetupService(refreshService, hijaxService, additionalSetupInitial, additionalSetup)
}