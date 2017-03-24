package org.danielnixon.progressive.services

import org.danielnixon.progressive.extensions.dom.{ EventTargetWrapper, NodeListSeq }
import org.danielnixon.progressive.shared.Wart
import org.danielnixon.progressive.shared.api.DataAttributes
import org.danielnixon.saferdom.{ Element, Event, MouseEvent, html }

class EventHandlerSetupService(
    refreshService: RefreshService,
    hijaxService: HijaxService,
    additionalSetupInitial: html.Body => Unit,
    additionalSetup: Element => Unit
) {

  def setup(element: Element): Unit = {
    additionalSetup(element)

    element.querySelectorAll(s"[${DataAttributes.refresh}]").collect({ case e: Element => e }) foreach { node =>
      refreshService.setupRefresh(node)
    }
  }

  @SuppressWarnings(Array(Wart.AsInstanceOf, Wart.Nothing))
  def setupInitial(body: html.Body): Unit = {

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

    setup(body)
  }
}
