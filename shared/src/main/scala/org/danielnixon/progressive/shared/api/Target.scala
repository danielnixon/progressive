package org.danielnixon.progressive.shared.api

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.deriveFor
import org.danielnixon.progressive.shared.Wart

sealed trait Target {
  override def toString: String
}

@SuppressWarnings(Array(Wart.AsInstanceOf))
object Target {

  case object Next extends Target

  case object Parent extends Target

  case object ChildTarget extends Target

  implicit val decoder: Decoder[Target] = deriveFor[Target].decoder
  implicit val encoder: Encoder[Target] = deriveFor[Target].encoder

  def asJson(target: Target): String = Json.asJson(target)

  def fromJson(json: String): Option[Target] = Json.fromJson(json)
}