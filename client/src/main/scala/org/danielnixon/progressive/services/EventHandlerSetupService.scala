package org.danielnixon.progressive.services

import org.danielnixon.progressive.extensions.dom.{ EventTargetWrapper, NodeListSeq }
import org.danielnixon.progressive.shared.Wart
import org.danielnixon.progressive.shared.api.DataAttributes
import org.danielnixon.saferdom.{ Element, Event, MouseEvent, html }

@SuppressWarnings(Array(Wart.AsInstanceOf))
class EventHandlerSetupService(
    additionalSetupInitial: html.Element => Unit,
    additionalSetup: Element => Unit
) {

  def setup(element: Element, refreshService: RefreshService): Unit = {
    additionalSetup(element)

    element.querySelectorAll(s"[${DataAttributes.refresh}]") foreach { node =>
      refreshService.setupRefresh(node.asInstanceOf[Element])
    }
  }

  @SuppressWarnings(Array(Wart.Nothing))
  def setupInitial(body: html.Element, refreshService: RefreshService, hijaxService: HijaxService): Unit = {

    additionalSetupInitial(body)

    body.on("click", s"a[${DataAttributes.progressive}]") { (e: MouseEvent, element: Element) =>
      hijaxService.ajaxLinkClick(e, element.asInstanceOf[html.Anchor])
    }

    body.on("click", s"form[${DataAttributes.progressive}] button[${DataAttributes.progressive}]") { (e: Event, element: Element) =>
      hijaxService.ajaxSubmitButtonClick(element.asInstanceOf[html.Button])
    }

    body.on("submit", s"form[${DataAttributes.progressive}]") { (e: Event, element: Element) =>
      hijaxService.ajaxFormSubmit(e, element.asInstanceOf[html.Form])
    }

    setup(body, refreshService)
  }
}
