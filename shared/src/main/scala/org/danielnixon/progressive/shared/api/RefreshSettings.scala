package org.danielnixon.progressive.shared.api

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto._
import org.danielnixon.progressive.shared.Wart

final case class RefreshSettings(url: String, interval: Option[Int])

@SuppressWarnings(Array(Wart.AsInstanceOf))
object RefreshSettings {
  implicit val decoder: Decoder[RefreshSettings] = deriveFor[RefreshSettings].decoder
  implicit val encoder: Encoder[RefreshSettings] = deriveFor[RefreshSettings].encoder

  def asJson(target: RefreshSettings): String = Json.asJson(target)

  def fromJson(json: String): Option[RefreshSettings] = Json.fromJson(json)
}
