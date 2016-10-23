package org.danielnixon.progressive.shared.api

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto._
import org.danielnixon.progressive.shared.Wart

final case class SubmitButtonSettings(
  target: Option[Target] = None,
  busyMessage: Option[String] = None
)

@SuppressWarnings(Array(Wart.AsInstanceOf))
object SubmitButtonSettings {
  implicit val decoder: Decoder[SubmitButtonSettings] = deriveFor[SubmitButtonSettings].decoder
  implicit val encoder: Encoder[SubmitButtonSettings] = deriveFor[SubmitButtonSettings].encoder

  def asJson(target: SubmitButtonSettings): String = Json.asJson(target)

  def fromJson(json: String): Option[SubmitButtonSettings] = Json.fromJson(json)
}