package org.danielnixon.progressive.services

import org.danielnixon.progressive.extensions.dom.NodeListSeq
import org.danielnixon.progressive.shared.Wart
import org.scalajs.dom.html.Element

import scalaz._
import Scalaz._

class EnableDisableService {

  def disable(element: Element): Unit = setDisabled(element, disabled = true)

  def enable(element: Element): Unit = setDisabled(element, disabled = false)

  private def setDisabled(element: Element, disabled: Boolean): Unit = {
    element.disabled = disabled
    foreachSubmitButton(element, _.disabled = disabled)
  }

  @SuppressWarnings(Array(Wart.AsInstanceOf))
  private def foreachSubmitButton(element: Element, op: (Element) => Unit): Unit = {
    if (element.nodeName === "FORM") {
      element.querySelectorAll("[type=submit]").foreach(submit => op(submit.asInstanceOf[Element]))
    }
  }
}
