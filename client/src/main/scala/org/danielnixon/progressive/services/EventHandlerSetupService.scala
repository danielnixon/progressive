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

    body.on("click", "a[data-ajax=true]", ((element: Element) =>
      hijaxService.ajaxLinkClick(jQuery(element))): js.ThisFunction0[Element, _])

    body.on("click", "form[data-ajax=true] button[type=submit]", ((element: Element) =>
      hijaxService.ajaxSubmitButtonClick(jQuery(element))): js.ThisFunction0[Element, _])

    body.on("submit", "form[data-ajax=true], form[data-confirm]", ((element: Element) =>
      hijaxService.ajaxFormSubmit(jQuery(element))): js.ThisFunction0[Element, _])

    setup(body, refreshService)
  }
}
