package org.danielnixon.progressive.extensions

import org.querki.jquery.JQuery
import org.scalajs.dom.html

import scala.scalajs.js

package object jquery {

  implicit class JQueryWrapper(private val jQuery: JQuery) extends AnyVal {
    def dataT[A](key: String)(implicit ev: A => js.Any): Option[A] = {
      jQuery.data(key).toOption.map(_.asInstanceOf[A])
    }
  }

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
