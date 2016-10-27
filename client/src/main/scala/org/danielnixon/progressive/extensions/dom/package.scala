package org.danielnixon.progressive.extensions

import org.danielnixon.progressive.facades.dom.ElementMatches.element2ElementMatches
import org.danielnixon.progressive.extensions.core.StringWrapper
import org.danielnixon.progressive.shared.Wart
import org.scalajs.dom._
import org.scalajs.dom.raw.NonDocumentTypeChildNode

import scalaz.{ Node => _, _ }
import Scalaz._
import scala.annotation.tailrec

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

    @SuppressWarnings(Array(Wart.AsInstanceOf))
    def parentOpt: Option[Element] = {
      Option(element.parentNode) match {
        case Some(parent) if parent.nodeType === Node.ELEMENT_NODE => Some(parent.asInstanceOf[Element])
        case _ => None
      }
    }

    def closest(selector: String): Option[Element] = {

      @tailrec
      def closestRec(elementOpt: Option[Element], selector: String): Option[Element] = {
        elementOpt match {
          case Some(e) => if (e.matches(selector)) Some(e) else closestRec(e.parentOpt, selector)
          case None => None
        }
      }

      closestRec(Some(element), selector)
    }
  }

  implicit class NonDocumentTypeChildNodeWrapper(val node: NonDocumentTypeChildNode) extends AnyVal {
    def nextElementSiblingOpt: Option[Element] = Option(node.nextElementSibling)
  }

  implicit class NodeSelectorWrapper(val element: NodeSelector) extends AnyVal {
    def querySelectorOpt(selectors: String): Option[Element] = Option(element.querySelector(selectors))
  }

}
