package org.danielnixon.progressive.extensions

import org.querki.jquery.JQuery
import org.scalajs.dom.html

package object jquery {

  implicit class JQuerySeq(jQuery: JQuery) extends IndexedSeq[html.Element] {
    override def foreach[U](f: html.Element => U): Unit = {
      for (i <- 0 until jQuery.length) {
        f(jQuery(i))
      }
    }

    override def length: Int = jQuery.length

    override def apply(idx: Int): html.Element = jQuery(idx)
  }

}
