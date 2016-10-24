package org.danielnixon.progressive.services

import org.querki.jquery._
import org.scalajs.dom.Element

import scala.scalajs.js

class EventHandlerSetupService(
    jQuery: JQueryStatic.type,
    additionalSetupInitial: JQuery => Unit,
    additionalSetup: JQuery => Unit
) {

  def setup(element: JQuery, refreshService: RefreshService): Unit = {

    additionalSetup(element)

    element.find("[data-refresh]").each { (element: Element) =>
      refreshService.setupRefresh(jQuery(element))
    }
  }

  def setupInitial(body: JQuery, refreshService: RefreshService, hijaxService: HijaxService): Unit = {

    additionalSetupInitial(body)

    body.on("click", "a[data-progressive]", js.undefined, (element: Element) =>
      hijaxService.ajaxLinkClick(jQuery(element)))

    body.on("click", "form[data-progressive] button[type=submit]", js.undefined, (element: Element) =>
      hijaxService.ajaxSubmitButtonClick(jQuery(element)))

    body.on("submit", "form[data-progressive]", js.undefined, (element: Element) =>
      hijaxService.ajaxFormSubmit(jQuery(element)))

    setup(body, refreshService)
  }
}
