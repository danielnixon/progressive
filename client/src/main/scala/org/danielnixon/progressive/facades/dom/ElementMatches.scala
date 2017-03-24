package org.danielnixon.progressive.facades.dom

import org.danielnixon.progressive.shared.Wart
import org.danielnixon.saferdom.Element

import scala.scalajs.js
import scala.language.implicitConversions

/**
  * Facade for matches.
  */
@js.native
trait ElementMatches extends js.Object {
  /**
    * @see https://developer.mozilla.org/en/docs/Web/API/Element/matches
    */
  def matches(selector: String): Boolean = js.native

  /**
    * @see https://developer.mozilla.org/en-US/docs/Web/API/Element/closest
    */
  def closest(selector: String): Element = js.native
}

object ElementMatches {
  @SuppressWarnings(Array(Wart.AsInstanceOf))
  implicit def element2ElementMatches(element: Element): ElementMatches = element.asInstanceOf[ElementMatches]
}