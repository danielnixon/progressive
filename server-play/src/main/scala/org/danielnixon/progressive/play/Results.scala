package org.danielnixon.progressive.play

import org.danielnixon.progressive.play.extensions.RequestHeaderWrapper
import play.api.mvc.{ Call, RequestHeader, Result }
import play.api.mvc.Results.Redirect

object Results {
  def redirectToRefererOrElse(call: Call)(implicit request: RequestHeader): Result = {
    request.referer.map(Redirect(_)).getOrElse(Redirect(call))
  }
}
