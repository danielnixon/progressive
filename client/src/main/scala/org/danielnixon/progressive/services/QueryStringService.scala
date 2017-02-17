package org.danielnixon.progressive.services

import org.danielnixon.saferdom.html.{ Element, Form, Input }
import org.danielnixon.progressive.shared.Wart

import scala.collection.immutable.Seq
import scalaz.Scalaz._

final case class QueryStringParam(name: String, value: Option[String])

class QueryStringService(formSerializer: FormSerializer) {
  def appendQueryString(path: String, search: String): String = {
    if (search.nonEmpty) path + "?" + search else path
  }

  def extractQueryStringParams(uri: String): Seq[QueryStringParam] = {
    val queryIndex = uri.indexOf("?")
    val queryString = if (queryIndex =/= -1) uri.substring(queryIndex + 1) else ""
    queryString.split("&").to[Seq].filter(_.nonEmpty).map { elem =>
      val pair = elem.split("=")
      QueryStringParam(pair(0), Option(pair(1)))
    }
  }

  def toQueryString(params: Seq[QueryStringParam]): String = {
    def hasValue(param: QueryStringParam) = {
      param.value.filter(v => v.nonEmpty && v =/= "false").map { v =>
        param.name -> v
      }
    }

    (for {
      (name, value) <- params.flatMap(hasValue)
    } yield name + "=" + value).mkString("&")
  }

  def updateQueryStringArray(existingParams: Seq[QueryStringParam], newParams: Seq[QueryStringParam]): String = {
    val paramsToRetain = existingParams.filter { existingParam =>
      !newParams.exists(_.name === existingParam.name)
    }

    toQueryString(paramsToRetain ++ newParams)
  }

  def updateQueryString(path: String, search: String, params: Seq[QueryStringParam]): String = {
    val existingParams = extractQueryStringParams(search)
    val newQueryString = updateQueryStringArray(existingParams, params)
    appendQueryString(path, newQueryString)
  }

  @SuppressWarnings(Array(Wart.AsInstanceOf))
  def paramsForQueryString(form: Form): Seq[QueryStringParam] = {

    def shouldInclude(element: Element) = {
      val isHiddenInputType = element match {
        case input: Input => input.`type` === "hidden"
        case _ => false
      }
      val isVisible = (element.offsetWidth gt 0D) || (element.offsetHeight gt 0D)
      isHiddenInputType || isVisible
    }

    formSerializer.serializeSeq(form) map {
      case (element, name, value) =>
        if (shouldInclude(element)) {
          QueryStringParam(name, Some(value))
        } else {
          QueryStringParam(name, None)
        }
    }
  }
}
