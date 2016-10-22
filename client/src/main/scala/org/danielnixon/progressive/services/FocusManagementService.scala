package org.danielnixon.progressive.services

import org.querki.jquery._
import org.scalajs.dom.Window

class FocusManagementService(window: Window, mainElement: JQuery) {

  def setFocus(element: JQuery): JQuery = {
    if (element.length > 0) {
      // Calling focus() can cause the page to jump around, even if the element being
      // focused is currently visible. Note the current offset and restore it after focusing.
      val originalX = window.pageXOffset
      val originalY = window.pageYOffset
      element.css("outline", "none").attr("tabindex", "-1").focus()
      window.scrollTo(originalX.toInt, originalY.toInt)

      // Now update the scroll position ourselves, ensuring that the top of the element
      // being focused is scrolled into view.
      val newY = element.offset.top - mainElement.offset.top - 10D
      val top = window.pageYOffset
      val bottom = top + window.innerHeight
      val shouldScroll = top > newY || newY > bottom
      if (shouldScroll) {
        window.scrollTo(0, newY.toInt)
      }
    }
    element
  }

  def anythingHasFocus = Option(window.document.querySelector(":focus")).isDefined
}
