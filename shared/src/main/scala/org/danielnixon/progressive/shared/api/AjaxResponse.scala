package org.danielnixon.progressive.shared.api

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.deriveFor
import org.danielnixon.progressive.shared.Wart

final case class AjaxResponse(message: Option[String], html: String)

@SuppressWarnings(Array(Wart.AsInstanceOf))
object AjaxResponse {

  implicit val decoder: Decoder[AjaxResponse] = deriveFor[AjaxResponse].decoder
  implicit val encoder: Encoder[AjaxResponse] = deriveFor[AjaxResponse].encoder

  def asJson(ajaxResponse: AjaxResponse): String = Json.asJson(ajaxResponse)

  def fromJson(json: String): Option[AjaxResponse] = Json.fromJson(json)
}