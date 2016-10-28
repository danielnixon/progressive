package org.danielnixon.progressive.shared.api

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import org.danielnixon.progressive.shared.Wart

final case class RefreshSettings(url: String, interval: Option[Int])

@SuppressWarnings(Array(Wart.AsInstanceOf, Wart.Nothing))
object RefreshSettings {
  implicit val decoder: Decoder[RefreshSettings] = deriveDecoder[RefreshSettings]
  implicit val encoder: Encoder[RefreshSettings] = deriveEncoder[RefreshSettings]

  def asJson(target: RefreshSettings): String = Json.asJson(target)

  def fromJson(json: String): Option[RefreshSettings] = Json.fromJson(json)
}
