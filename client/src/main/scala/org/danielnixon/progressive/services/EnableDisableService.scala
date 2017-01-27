package org.danielnixon.progressive.services

import org.danielnixon.progressive.extensions.dom.NodeListSeq
import org.danielnixon.saferdom.Element
import org.danielnixon.saferdom.html.{ Button, Form, Input }

import scalaz._
import Scalaz._

class EnableDisableService {

  def disable(element: Element): Unit = setDisabled(element, disabled = true)

  def enable(element: Element): Unit = setDisabled(element, disabled = false)

  private def setDisabled(element: Element, disabled: Boolean): Unit = {

    def setDisabled(element: Element): Unit = {
      element match {
        case e: Button if e.`type` === "submit" => e.disabled = disabled
        case e: Input if e.`type` === "submit" => e.disabled = disabled
        case _ =>
      }
    }

    element match {
      case e: Form => e.elements.foreach(setDisabled)
      case e => setDisabled(e)
    }
  }
}
