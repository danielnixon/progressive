package org.danielnixon.progressive.services

import org.danielnixon.progressive.extensions.virtualdom.PatchObjectWrapper
import org.danielnixon.progressive.facades.virtualdom.{ VDomParser, VTree, VirtualDom }
import org.scalajs.dom.html.Element

class VDomService(virtualDom: VirtualDom, vdomParser: VDomParser) {
  def update(element: Element, html: String): VTree = {
    val targetVdom = vdomParser(element)
    val newVdom = vdomParser(html)
    val patchObject = virtualDom.diff(targetVdom, newVdom)

    if (!patchObject.isEmpty) {
      virtualDom.patch(element, patchObject)
    }

    newVdom
  }
}
