package org.danielnixon.progressive.shared.api

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import org.danielnixon.progressive.shared.Wart

final case class LinkSettings(
  target: Option[Target] = None,
  busyMessage: Option[String] = None,
  href: Option[String]
)

@SuppressWarnings(Array(Wart.AsInstanceOf, Wart.Nothing))
object LinkSettings {
  implicit val decoder: Decoder[LinkSettings] = deriveDecoder[LinkSettings]
  implicit val encoder: Encoder[LinkSettings] = deriveEncoder[LinkSettings]

  def asJson(target: LinkSettings): String = Json.asJson(target)

  def fromJson(json: String): Option[LinkSettings] = Json.fromJson(json)
}