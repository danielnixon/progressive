package org.danielnixon.progressive.facades.virtualdom

import org.scalajs.dom.html

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

/**
  * Facade for vdom-parser.
  * @see https://github.com/bitinn/vdom-parser
  */
@js.native
@JSGlobal
class VDomParser extends js.Object {
  def apply(element: html.Element): VTree = js.native

  def apply(element: String): VTree = js.native
}

