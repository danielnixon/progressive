package org.danielnixon.progressive

import org.danielnixon.progressive.shared.http.{ HeaderNames, HeaderValues }
import play.api.mvc.RequestHeader

package object extensions {
  implicit class RequestHeaderWrapper(val request: RequestHeader) extends AnyVal {
    def isAjax: Boolean = request.headers.get(HeaderNames.X_REQUESTED_WITH).contains(HeaderValues.XML_HTTP_REQUEST)
  }
}
