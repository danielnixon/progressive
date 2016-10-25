package org.danielnixon.progressive.services

import org.querki.jquery._
import org.scalajs.dom.Element

import scala.scalajs.js

class EventHandlerSetupService(
    jQuery: JQueryStatic.type,
    additionalSetupInitial: Element => Unit,
    additionalSetup: Element => Unit
) {

  def setup(element: Element, refreshService: RefreshService): Unit = {

    additionalSetup(element)

    jQuery(element).find("[data-refresh]").each(refreshService.setupRefresh _)
  }

  def setupInitial(body: Element, refreshService: RefreshService, hijaxService: HijaxService): Unit = {

    additionalSetupInitial(body)

    val jQueryBody = jQuery(body)

    jQueryBody.on("click", "a[data-progressive]", js.undefined, (element: Element) =>
      hijaxService.ajaxLinkClick(jQuery(element)))

    jQueryBody.on("click", "form[data-progressive] button[type=submit]", js.undefined, (element: Element) =>
      hijaxService.ajaxSubmitButtonClick(jQuery(element)))

    jQueryBody.on("submit", "form[data-progressive]", js.undefined, (element: Element) =>
      hijaxService.ajaxFormSubmit(jQuery(element)))

    setup(body, refreshService)
  }
}
