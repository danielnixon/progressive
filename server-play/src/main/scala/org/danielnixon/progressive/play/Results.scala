package org.danielnixon.progressive.play

import java.net.URI

import org.danielnixon.progressive.play.extensions.RequestHeaderWrapper
import play.api.mvc.{ Call, RequestHeader, Result }
import play.api.mvc.Results.Redirect

import scala.util.Try
import scalaz.Scalaz._

object Results {
  def redirectToRefererOrElse(call: Call)(implicit request: RequestHeader): Result = {
    request.referer.
      flatMap(r => Try(new URI(r)).toOption).
      filter(r => !r.isAbsolute || request.domain === r.getHost).
      map(r => Redirect(r.toString)).
      getOrElse(Redirect(call))
  }
}
