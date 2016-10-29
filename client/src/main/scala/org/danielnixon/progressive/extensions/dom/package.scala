package org.danielnixon.progressive.extensions

import org.danielnixon.progressive.facades.dom.ElementMatches.element2ElementMatches
import org.danielnixon.progressive.extensions.core.StringWrapper
import org.danielnixon.progressive.shared.Wart
import org.scalajs.dom._
import org.scalajs.dom.raw.{ Event, NonDocumentTypeChildNode }

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

    def parentOpt: Option[Element] = {
      Option(element.parentNode) match {
        case Some(parent: Element) => Some(parent)
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

  implicit class EventTargetWrapper(val eventTarget: EventTarget) extends AnyVal {

    @SuppressWarnings(Array(Wart.Nothing))
    def on[T <: Event](events: String, selector: String)(handler: (T, Element) => Unit): Unit = {
      eventTarget.addEventListener(events, (event: T) => {

        event.target match {
          case element: Element if element.matches(selector) => handler(event, element)
          case _ =>
        }
      })
    }
  }

}
