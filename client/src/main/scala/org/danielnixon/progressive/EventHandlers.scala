package org.danielnixon.progressive

import org.danielnixon.progressive.extensions.dom.NodeListSeq
import org.danielnixon.progressive.shared.Wart
import org.danielnixon.saferdom.{ Element, html }
import org.danielnixon.saferdom.html.{ Body, Form }
import org.danielnixon.saferdom.implicits._

/**
  * Extension points to customize the behaviour of Progressive. You'll need to provide an implementation of
  * this trait when initializing Progressive.
  */
trait EventHandlers {
  /**
    * Use this method to set up the body of the page by attaching event handlers, progressively enhancing
    * elements, etc. This method is invoked by Progressive exactly once per page load.
    * @param body The body element.
    */
  def additionalSetupInitial(body: Body): Unit = ()

  /**
    * Use this method to set up page elements that have been updated by ajax forms, links or refresh elements.
    * It is important that you keep this method idempotent and performant as it may be invoked frequently if you
    * have refresh elements set to refresh on a schedule.
    * @param element The element being set up.
    */
  def additionalSetup(element: Element): Unit = ()

  /**
    * Code to execute before an ajax form has been submitted.
    * The default implementation resets any invalid form elements.
    * @param form The form.
    * @return True if the form should be submitted, false otherwise.
    */
  @SuppressWarnings(Array(Wart.AsInstanceOf))
  def preFormSubmit(form: Form): Boolean = {
    // Reset invalid form elements before resubmitting form.
    form.querySelectorAll("[aria-invalid=true]") foreach { node =>
      node.asInstanceOf[html.Element].removeAttribute("aria-invalid")
    }

    true
  }

  /**
    * Code to execute after an ajax form has been submitted.
    * The default implementation focuses the first invalid form element (if any).
    * @param form The form.
    */
  @SuppressWarnings(Array(Wart.AsInstanceOf))
  def postFormSubmit(form: Form): Unit = {
    // Focus the first invalid form element (if any).
    form.querySelector("[aria-invalid=true]") foreach { node =>
      node.asInstanceOf[html.Element].focus()
    }
  }

  /**
    * Determines whether a virtual dom patch should be applied to a refresh element or not.
    * @param element The refresh element.
    * @return True if the virtual dom patch should be applied, false otherwise.
    */
  def applyDiff(element: Element): Boolean = true

  /**
    * A y-axis offset (in pixels) to use when scrolling an element into view. Useful when there
    * are elements fixed at the top of the page, such as navbars or headers, that would otherwise
    * obscure the element being scrolled into view.
    * @return The offset in pixels to apply when scrolling an element into view.
    */
  def scrollOffset: Int = 0
}
