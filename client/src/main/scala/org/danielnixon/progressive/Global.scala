package org.danielnixon.progressive

import org.danielnixon.progressive.facades.virtualdom._
import shared.Wart

import scala.scalajs.js

@SuppressWarnings(Array(Wart.AsInstanceOf))
object Global {
  val virtualDom: VirtualDom = js.Dynamic.global.virtualDom.asInstanceOf[VirtualDom]
  val vdomParser: VDomParser = js.Dynamic.global.vdomParser.asInstanceOf[VDomParser]
}
