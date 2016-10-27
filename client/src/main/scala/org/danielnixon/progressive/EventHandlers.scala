package org.danielnixon.progressive

import org.scalajs.dom.Element

trait EventHandlers {
  def additionalSetupInitial(body: Element): Unit

  def additionalSetup(element: Element): Unit

  def preFormSubmit(form: Element): Boolean

  def postFormSubmit(form: Element): Unit

  def applyDiff(element: Element): Boolean

  def includeInQueryString(element: Element): Boolean
}
