package org.danielnixon.progressive.services

import org.danielnixon.progressive.extensions.dom.NodeListSeq
import org.scalajs.dom.Element
import org.scalajs.dom.html.{ DataList, Input, Option => OptionElement }
import org.danielnixon.progressive.shared.Wart

import scala.collection.immutable.Seq
import scala.scalajs.js.URIUtils
import scalaz.Scalaz._

final case class QueryStringParam(name: String, value: Option[String])

class QueryStringService(includeInQueryString: Input => Boolean) {
  def appendQueryString(path: String, search: String): String = {
    if (search.nonEmpty) path + '?' + search else path
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
      param.value.filter(v => v.nonEmpty && v != "false").map { v =>
        param.name -> v
      }
    }

    (for {
      (name, value) <- params.flatMap(hasValue)
    } yield name + '=' + value).mkString("&")
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
  def paramsForQueryString(form: Element): Seq[QueryStringParam] = {

    form.querySelectorAll("textarea[name], input[name], select[name]").to[Seq].flatMap { e =>
      val input = e.asInstanceOf[Input]

      val shouldInclude = {
        val isHiddenInputType = input.`type` === "hidden"
        val isVisible = (input.offsetWidth gt 0D) || (input.offsetHeight gt 0D)
        isHiddenInputType || isVisible
      }

      val values = if (shouldInclude) {
        if (input.multiple) {
          input.asInstanceOf[DataList].options.
            map(_.asInstanceOf[OptionElement]).
            filter(_.selected).
            map(optionElem => Some(optionElem.value))
        } else {
          if (!includeInQueryString(input)) {
            Seq(None)
          } else {
            Seq(Some(input.value))
          }
        }
      } else {
        Seq(None)
      }

      values.map { value =>
        QueryStringParam(input.name, value.map(URIUtils.encodeURIComponent))
      }
    }
  }
}
