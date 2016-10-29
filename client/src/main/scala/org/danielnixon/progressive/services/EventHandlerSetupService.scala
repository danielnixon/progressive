package org.danielnixon.progressive.services

import org.danielnixon.progressive.extensions.dom.{ EventTargetWrapper, NodeListSeq }
import org.danielnixon.progressive.shared.Wart
import org.scalajs.dom.{ Element, Event, html }

@SuppressWarnings(Array(Wart.AsInstanceOf))
class EventHandlerSetupService(
    additionalSetupInitial: Element => Unit,
    additionalSetup: Element => Unit
) {

  def setup(element: Element, refreshService: RefreshService): Unit = {
    additionalSetup(element)

    element.querySelectorAll("[data-refresh]") foreach { node =>
      refreshService.setupRefresh(node.asInstanceOf[Element])
    }
  }

  @SuppressWarnings(Array(Wart.Nothing))
  def setupInitial(body: Element, refreshService: RefreshService, hijaxService: HijaxService): Unit = {

    additionalSetupInitial(body)

    body.on("click", "a[data-progressive]") { (e: Event, element: Element) =>
      hijaxService.ajaxLinkClick(e, element.asInstanceOf[html.Anchor])
    }

    body.on("click", "form[data-progressive] button[data-progressive]") { (e: Event, element: Element) =>
      hijaxService.ajaxSubmitButtonClick(element.asInstanceOf[html.Button])
    }

    body.on("submit", "form[data-progressive]") { (e: Event, element: Element) =>
      hijaxService.ajaxFormSubmit(e, element.asInstanceOf[html.Form])
    }

    setup(body, refreshService)
  }
}
