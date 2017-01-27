package org.danielnixon.progressive.services

import org.danielnixon.progressive.shared.Wart

import scala.collection.immutable.Seq
import org.danielnixon.saferdom.document
import org.danielnixon.saferdom.html.Form
import org.scalatest.{ Matchers, WordSpec }

class QueryStringServiceSpec extends WordSpec with Matchers {
  "paramsForQueryString" should {
    "return empty array when no inputs" in {
      @SuppressWarnings(Array(Wart.AsInstanceOf))
      val form = document.createElement("form").asInstanceOf[Form]
      val result = new QueryStringService(new FormSerializer).paramsForQueryString(form)
      result shouldEqual Nil
    }
  }

  "appendQueryString" should {
    "append non-empty query string" in {
      val result = new QueryStringService(new FormSerializer).appendQueryString("foo", "bar")
      result shouldEqual "foo?bar"
    }

    "not append empty query string" in {
      val result = new QueryStringService(new FormSerializer).appendQueryString("foo", "")
      result shouldEqual "foo"
    }
  }

  "toQueryString" should {
    "filter empty string" in {
      val result = new QueryStringService(new FormSerializer).toQueryString(Seq(QueryStringParam("foo", Some(""))))
      result shouldEqual ""
    }

    "filter false string" in {
      val result = new QueryStringService(new FormSerializer).toQueryString(Seq(QueryStringParam("foo", Some("false"))))
      result shouldEqual ""
    }

    "include non-empty string" in {
      val result = new QueryStringService(new FormSerializer).toQueryString(Seq(QueryStringParam("foo", Some("bar"))))
      result shouldEqual "foo=bar"
    }
  }

  "extractQueryStringParams" should {
    "extract empty query string" in {
      val result = new QueryStringService(new FormSerializer).extractQueryStringParams("http://localhost/")
      result shouldEqual Nil
    }

    "extract non-empty query string" in {
      val result = new QueryStringService(new FormSerializer).extractQueryStringParams("http://localhost/?foo=bar&baz=qux")
      result shouldEqual Seq(
        QueryStringParam("foo", Some("bar")),
        QueryStringParam("baz", Some("qux"))
      )
    }

    "extract non-empty query string with empty path" in {
      val result = new QueryStringService(new FormSerializer).extractQueryStringParams("?foo=bar&baz=qux")
      result shouldEqual Seq(
        QueryStringParam("foo", Some("bar")),
        QueryStringParam("baz", Some("qux"))
      )
    }
  }

  "updateQueryString" should {
    "update empty query string with empty array" in {
      val result = new QueryStringService(new FormSerializer).updateQueryString("", "", Nil)
      result shouldEqual ""
    }

    "update empty query string with non-empty array" in {
      val result = new QueryStringService(new FormSerializer).updateQueryString("", "", Seq(
        QueryStringParam("foo", Some("bar")),
        QueryStringParam("baz", Some("qux"))
      ))
      result shouldEqual "?foo=bar&baz=qux"
    }

    "update non-empty query string with non-empty array" in {
      val result = new QueryStringService(new FormSerializer).updateQueryString("", "?foo=bar&baz=qux", Seq(
        QueryStringParam("baz", Some("qux"))
      ))
      result shouldEqual "?foo=bar&baz=qux"
    }

    "update non-empty query string with empty array" in {
      val result = new QueryStringService(new FormSerializer).updateQueryString("", "?foo=bar&baz=qux", Nil)
      result shouldEqual "?foo=bar&baz=qux"
    }

    "update existing values" in {
      val result = new QueryStringService(new FormSerializer).updateQueryString("", "?foo=bar&baz=qux", Seq(
        QueryStringParam("foo", Some("BAR")),
        QueryStringParam("baz", Some("QUX"))
      ))
      result shouldEqual "?foo=BAR&baz=QUX"
    }

    "remove empty values" in {
      val result = new QueryStringService(new FormSerializer).updateQueryString("", "?foo=bar&baz=qux", Seq(
        QueryStringParam("foo", None),
        QueryStringParam("baz", Some("QUX"))
      ))
      result shouldEqual "?baz=QUX"
    }

    "remove false values" in {
      val result = new QueryStringService(new FormSerializer).updateQueryString("", "?foo=bar&baz=qux", Seq(
        QueryStringParam("foo", Some("false")),
        QueryStringParam("baz", Some("QUX"))
      ))
      result shouldEqual "?baz=QUX"
    }
  }
}
