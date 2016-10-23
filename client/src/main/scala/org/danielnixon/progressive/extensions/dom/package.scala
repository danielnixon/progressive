package org.danielnixon.progressive.extensions

import org.danielnixon.progressive.extensions.core.StringWrapper
import org.scalajs.dom._

package object dom {

  // https://www.scala-js.org/doc/sjs-for-js/es6-to-scala-part3.html
  implicit class NodeListSeq[T <: Node](nodes: DOMList[T]) extends IndexedSeq[T] {
    override def foreach[U](f: T => U): Unit = {
      for (i <- 0 until nodes.length) {
        f(nodes(i))
      }
    }

    override def length: Int = nodes.length

    override def apply(idx: Int): T = nodes(idx)
  }

  implicit class ElementWrapper(val element: Element) extends AnyVal {
    def getAttributeOpt(name: String): Option[String] = element.getAttribute(name).toOption
  }

}
