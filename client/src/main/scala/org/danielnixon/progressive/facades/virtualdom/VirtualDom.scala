package org.danielnixon.progressive.facades.virtualdom

import org.scalajs.dom.Node

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

/**
  * Facade for virtual-dom.
  * @see https://github.com/Matt-Esch/virtual-dom
  */
@js.native
@JSGlobal
class VirtualDom extends js.Object {
  def diff(previous: VTree, current: VTree): PatchObject = js.native

  def patch(rootNode: Node, patches: PatchObject): Node = js.native
}

@js.native
@JSGlobal
class VTree extends js.Object

@js.native
@JSGlobal
class PatchObject extends js.Object
