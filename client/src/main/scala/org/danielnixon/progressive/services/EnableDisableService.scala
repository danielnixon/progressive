package org.danielnixon.progressive.services

import org.danielnixon.progressive.extensions.dom.NodeListSeq
import org.danielnixon.progressive.shared.Wart
import org.scalajs.dom.html.Element

import scalaz._
import Scalaz._

class EnableDisableService {

  @SuppressWarnings(Array(Wart.AsInstanceOf))
  def disable(element: Element): Unit = {
    element.disabled = true

    if (element.nodeName === "FORM") {
      element.querySelectorAll("[type=submit]").foreach(submit => disable(submit.asInstanceOf[Element]))
    }
  }

  @SuppressWarnings(Array(Wart.AsInstanceOf))
  def enable(element: Element): Unit = {
    element.disabled = false

    if (element.nodeName === "FORM") {
      element.querySelectorAll("[type=submit]").foreach(submit => enable(submit.asInstanceOf[Element]))
    }
  }
}
