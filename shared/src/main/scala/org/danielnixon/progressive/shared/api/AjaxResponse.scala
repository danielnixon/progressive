package org.danielnixon.progressive.shared.api

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import org.danielnixon.progressive.shared.Wart

/**
  * A response to an asynchronous request made by Progressive.
  * @param message A message to announce to users of assistive technology.
  * @param html An HTML fragment to render in the target element.
  */
final case class AjaxResponse(message: Option[String], html: Option[String])

@SuppressWarnings(Array(Wart.AsInstanceOf, Wart.Nothing))
object AjaxResponse {

  implicit val decoder: Decoder[AjaxResponse] = deriveDecoder[AjaxResponse]
  implicit val encoder: Encoder[AjaxResponse] = deriveEncoder[AjaxResponse]

  def asJson(ajaxResponse: AjaxResponse): String = Json.asJson(ajaxResponse)

  def fromJson(json: String): Option[AjaxResponse] = Json.fromJson(json)
}