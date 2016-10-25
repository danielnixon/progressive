package org.danielnixon.progressive

import org.scalajs.dom.Element
import org.scalajs.dom.html.Input

trait EventHandlers {
  def additionalSetupInitial(body: Element): Unit

  def additionalSetup(element: Element): Unit

  def preFormSubmit(form: Element): Boolean

  def postFormSubmit(form: Element): Unit

  def applyDiff(element: Element): Boolean

  def includeInQueryString(input: Input): Boolean
}
