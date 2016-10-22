package org.danielnixon.progressive

import org.querki.jquery.JQuery

trait EventHandlers {
  def additionalSetupInitial(body: JQuery): Unit

  def additionalSetup(element: JQuery): Unit

  def preFormSubmit(form: JQuery): Unit

  def postFormSubmit(form: JQuery): Unit
}
