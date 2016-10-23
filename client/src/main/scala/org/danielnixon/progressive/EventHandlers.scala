package org.danielnixon.progressive

import org.querki.jquery.JQuery

trait EventHandlers {
  def additionalSetupInitial(body: JQuery): Unit

  def additionalSetup(element: JQuery): Unit

  def preFormSubmit(form: JQuery): Boolean

  def postFormSubmit(form: JQuery): Unit

  def applyDiff(element: JQuery): Boolean
}
