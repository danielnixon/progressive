package org.danielnixon.progressive.facades.virtualdom

import org.danielnixon.saferdom.html

import scala.scalajs.js

/**
  * Facade for vdom-parser.
  * @see https://github.com/bitinn/vdom-parser
  */
@js.native
class VDomParser extends js.Object {
  def apply(element: html.Element): VTree = js.native

  def apply(element: String): VTree = js.native
}

