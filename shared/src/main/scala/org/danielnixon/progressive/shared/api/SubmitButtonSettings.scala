package org.danielnixon.progressive.shared.api

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import org.danielnixon.progressive.shared.Wart

final case class SubmitButtonSettings(
  target: Option[Target] = None,
  busyMessage: Option[String] = None
)

@SuppressWarnings(Array(Wart.Nothing))
object SubmitButtonSettings {
  implicit val decoder: Decoder[SubmitButtonSettings] = deriveDecoder[SubmitButtonSettings]
  implicit val encoder: Encoder[SubmitButtonSettings] = deriveEncoder[SubmitButtonSettings]

  def asJson(target: SubmitButtonSettings): String = Json.asJson(target)

  def fromJson(json: String): Option[SubmitButtonSettings] = Json.fromJson(json)
}