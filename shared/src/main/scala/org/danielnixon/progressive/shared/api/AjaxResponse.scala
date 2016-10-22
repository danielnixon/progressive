package org.danielnixon.progressive.shared.api

import io.circe.{ Decoder, Encoder }
import io.circe.generic.auto._
import org.danielnixon.progressive.shared.Wart

final case class AjaxResponse(message: Option[String], html: String)

object AjaxResponse {
  @SuppressWarnings(Array(Wart.AsInstanceOf))
  def asJson(ajaxResponse: AjaxResponse): String = Json.asJson(ajaxResponse)(implicitly[Encoder[AjaxResponse]])

  @SuppressWarnings(Array(Wart.AsInstanceOf))
  def fromJson(json: String): Option[AjaxResponse] = Json.fromJson(json)(implicitly[Decoder[AjaxResponse]])
}