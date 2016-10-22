package org.danielnixon.progressive.facades.virtualdom

import org.scalajs.dom.html

import scala.scalajs.js

/**
  * See https://github.com/bitinn/vdom-parser
  */
@js.native
class VDomParser extends js.Object {
  def apply(element: html.Element): VTree = js.native

  def apply(element: String): VTree = js.native
}

