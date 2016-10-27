package org.danielnixon.progressive.services

import org.danielnixon.progressive.extensions.dom.NodeListSeq
import org.danielnixon.progressive.facades.dom.ElementMatches.element2ElementMatches
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

    body.addEventListener("click", (e: Event) => {

      val element = e.target.asInstanceOf[Element]

      if (element.matches("a[data-progressive]")) {
        hijaxService.ajaxLinkClick(e, element.asInstanceOf[html.Anchor])
      } else if (element.matches("form[data-progressive] button[data-progressive]")) {
        hijaxService.ajaxSubmitButtonClick(element.asInstanceOf[html.Button])
      }

    })

    body.addEventListener("submit", (e: Event) => {
      val element = e.target.asInstanceOf[Element]

      if (element.matches("form[data-progressive]")) {
        hijaxService.ajaxFormSubmit(e, element.asInstanceOf[html.Form])
      }
    })

    setup(body, refreshService)
  }
}
