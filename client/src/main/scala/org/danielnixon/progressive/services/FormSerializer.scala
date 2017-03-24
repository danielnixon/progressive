package org.danielnixon.progressive.services

import org.danielnixon.progressive.extensions.dom.NodeListSeq
import org.danielnixon.saferdom.html.{ Button, Element, Form, Input, Select, TextArea }

import scalaz._
import Scalaz._
import scala.collection.immutable.Seq
import scala.scalajs.js.URIUtils

class FormSerializer {
  // scalastyle:off cyclomatic.complexity
  def serializeSeq(form: Form): Seq[(Element, String, String)] = {
    form.elements.to[Seq] flatMap {
      case x: Select if x.name.nonEmpty && !x.disabled =>
        if (x.multiple) {
          x.options.filter(_.selected).map(opt => (x, x.name, opt.value))
        } else {
          Seq((x, x.name, x.value))
        }
      case x: Input if x.name.nonEmpty && !x.disabled =>
        if (x.`type` === "checkbox" || x.`type` === "radiobutton") {
          if (x.checked) Seq((x, x.name, x.value)) else Nil
        } else {
          Seq((x, x.name, x.value))
        }
      case x: Input if x.name.nonEmpty && !x.disabled =>
        if (x.`type` =/= "file") Seq((x, x.name, x.value)) else Nil
      case x: TextArea if x.name.nonEmpty && !x.disabled =>
        Seq((x, x.name, x.value))
      case x: Button if x.name.nonEmpty && !x.disabled =>
        Seq((x, x.name, x.value))
      case _ => Nil
    }
  }
  // scalastyle:on cyclomatic.complexity

  def serialize(form: Form): String = {
    serializeSeq(form).map({ case (_, name, value) => name + "=" + URIUtils.encodeURIComponent(value) }).mkString("&")
  }
}
