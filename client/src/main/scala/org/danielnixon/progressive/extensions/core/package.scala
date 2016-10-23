package org.danielnixon.progressive.extensions

package object core {
  implicit class StringWrapper(val string: String) extends AnyVal {
    def toOption: Option[String] = Option(string).filter(_.nonEmpty)
  }
}
