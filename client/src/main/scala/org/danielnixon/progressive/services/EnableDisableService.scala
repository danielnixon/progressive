package org.danielnixon.progressive.services

import org.querki.jquery._

class EnableDisableService {

  def disable(element: JQuery): JQuery = {
    element.addClass("disabled").attr("aria-disabled", "true")
    if (element.is("form")) {
      disable(element.find("[type=submit]"))
    }
    element
  }

  def enable(element: JQuery): JQuery = {
    element.removeClass("disabled").removeAttr("aria-disabled")
    if (element.is("form")) {
      enable(element.find("[type=submit]"))
    }
    element
  }

  def isDisabled(element: JQuery): Boolean = {
    element.hasClass("disabled")
  }
}
