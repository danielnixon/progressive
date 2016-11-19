package org.danielnixon.progressive

import org.scalajs.dom.Element
import org.scalajs.dom.html.Form
import org.scalajs.dom.raw.HTMLElement

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
  def additionalSetupInitial(body: HTMLElement): Unit = ()

  /**
    * Use this method to set up page elements that have been updated by ajax forms, links or refresh elements.
    * It is important that you keep this method idempotent and performant as it may be invoked frequently if you
    * have refresh elements set to refresh on a schedule.
    * @param element The element being set up.
    */
  def additionalSetup(element: Element): Unit = ()

  /**
    * Code to execute before an ajax form has been submitted.
    * @param form The form.
    * @return True if the form should be submitted, false otherwise.
    */
  def preFormSubmit(form: Form): Boolean = true

  /**
    * Code to execute after an ajax form has been submitted.
    * @param form The form.
    */
  def postFormSubmit(form: Form): Unit = ()

  /**
    * Determines whether a virtual dom patch should be applied to a refresh element or not.
    * @param element The refresh element.
    * @return True if the virtual dom patch should be applied, false otherwise.
    */
  def applyDiff(element: Element): Boolean = true

  /**
    * When submitting an ajax form that has a `get` method, Progressive updates the query string to reflect
    * the form submission. You may want to exclude some form elements from the query string, for example if their value
    * is the default value for that query string parameter. Note that these elements won't be excluded if JavaScript is
    * unavailable, so they must only be excluded for cosmetic reasons.
    * @param element The form element (input, select, textarea or button).
    * @return True if the form element should be included in the query string, false otherwise.
    */
  def includeInQueryString(element: Element): Boolean = true

  /**
    * A y-axis offset (in pixels) to use when scrolling an element into view. Useful when there
    * are elements fixed at the top of the page, such as navbars or headers, that would otherwise
    * obscure the element being scrolled into view.
    * @return The offset in pixels to apply when scrolling an element into view.
    */
  def scrollOffset: Int = 0
}
