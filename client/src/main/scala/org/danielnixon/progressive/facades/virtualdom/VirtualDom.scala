package org.danielnixon.progressive.facades.virtualdom

import org.scalajs.dom.Node

import scala.scalajs.js

/**
  * See https://github.com/Matt-Esch/virtual-dom
  */
@js.native
class VirtualDom extends js.Object {
  def diff(previous: VTree, current: VTree): PatchObject = js.native

  def patch(rootNode: Node, patches: PatchObject): Node = js.native
}

@js.native
class VTree extends js.Object

@js.native
class PatchObject extends js.Object
