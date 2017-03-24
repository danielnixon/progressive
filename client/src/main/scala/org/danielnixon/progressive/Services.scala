package org.danielnixon.progressive

import org.danielnixon.progressive.services._
import org.danielnixon.saferdom

trait Services {

  def userAgentService: UserAgentService

  def historyService: HistoryService

  def eventHandlerSetupService: EventHandlerSetupService

  def eventService: EventService

  def focusManagementService: FocusManagementService

  def ajaxService: AjaxService

  def targetService: TargetService

  def refreshService: RefreshService

  def hijaxService: HijaxService
}

class DefaultServices(views: Views, elements: KeyElements, eventHandlers: EventHandlers) extends Services {

  override val userAgentService: UserAgentService = new UserAgentService(saferdom.window)

  override val historyService = new HistoryService(saferdom.window)

  val formSerializer = new FormSerializer

  override val eventService: EventService = new EventService

  override val focusManagementService: FocusManagementService =
    new FocusManagementService(saferdom.window, eventHandlers.scrollOffset _, userAgentService)

  override val ajaxService: AjaxService = new AjaxService

  override val targetService: TargetService = new TargetService

  override val refreshService: RefreshService =
    new RefreshService(Global.virtualDom, Global.vdomParser, eventHandlerSetupService, ajaxService, eventHandlers.applyDiff)

  override val hijaxService: HijaxService = {
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
      eventHandlers.preFormSubmit,
      eventHandlers.postFormSubmit
    )
  }

  override lazy val eventHandlerSetupService: EventHandlerSetupService =
    new EventHandlerSetupService(refreshService, hijaxService, eventHandlers.additionalSetupInitial, eventHandlers.additionalSetup)
}