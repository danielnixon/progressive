package org.danielnixon.progressive.extensions

import org.danielnixon.progressive.facades.dom.ElementMatches
import org.danielnixon.progressive.shared.Wart
import org.danielnixon.saferdom.{ DOMList, Element, EventTarget, Node }
import org.danielnixon.saferdom.implicits.saferDOMList
import org.danielnixon.saferdom.raw.Event

package object dom {

  /**
    * Treat DOMList as a seq.
    * @see https://www.scala-js.org/doc/sjs-for-js/es6-to-scala-part3.html
    */
  implicit class NodeListSeq[T <: Node](nodes: DOMList[T]) extends IndexedSeq[T] {

    @SuppressWarnings(Array(Wart.OptionPartial))
    override def foreach[U](f: T => U): Unit = {
      for (i <- 0 until nodes.length) {
        f(nodes.item(i).get)
      }
    }

    override def length: Int = nodes.length

    @SuppressWarnings(Array(Wart.Throw))
    override def apply(idx: Int): T = {
      nodes.item(idx).getOrElse(throw new IndexOutOfBoundsException(idx.toString))
    }
  }

  implicit class ElementWrapper(val element: Element) extends AnyVal {
    def closest(selector: String): Option[Element] = {
      Option(ElementMatches.element2ElementMatches(element).closest(selector))
    }
  }

  implicit class EventTargetWrapper(val eventTarget: EventTarget) extends AnyVal {

    @SuppressWarnings(Array(Wart.Nothing))
    def on[T <: Event](events: String, selector: String)(handler: (T, Element) => Unit): Unit = {

      val listener = (event: T) => {
        event.target match {
          case element: Element => element.closest(selector).foreach(closest => handler(event, closest))
          case _ =>
        }
      }

      eventTarget.addEventListener(events, listener)
    }
  }

}
