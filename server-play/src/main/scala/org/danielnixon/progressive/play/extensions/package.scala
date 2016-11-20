package org.danielnixon.progressive.play

import org.danielnixon.progressive.shared.http.{ HeaderNames, HeaderValues }
import play.api.http.HeaderNames.REFERER
import play.api.mvc.{ RequestHeader, Result }

package object extensions {

  implicit class ResultWrapper(val result: Result) extends AnyVal {
    def flashingError(message: String): Result = result.flashing(FlashKeys.error -> message)

    def flashingSuccess(message: String): Result = result.flashing(FlashKeys.success -> message)
  }

  implicit class RequestHeaderWrapper(val request: RequestHeader) extends AnyVal {
    def isAjax: Boolean = request.headers.get(HeaderNames.X_REQUESTED_WITH).contains(HeaderValues.XML_HTTP_REQUEST)

    def referer: Option[String] = request.headers.get(REFERER)

    def flashErrorMessage: Option[String] = request.flash.get(FlashKeys.error)

    def flashSuccessMessage: Option[String] = request.flash.get(FlashKeys.success)
  }
}
