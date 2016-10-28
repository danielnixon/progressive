package org.danielnixon.progressive.shared.api

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import org.danielnixon.progressive.shared.Wart

sealed trait Target {
  override def toString: String
}

@SuppressWarnings(Array(Wart.AsInstanceOf, Wart.Nothing))
object Target {

  case object Next extends Target

  case object Parent extends Target

  case object ChildTarget extends Target

  implicit val decoder: Decoder[Target] = deriveDecoder[Target]
  implicit val encoder: Encoder[Target] = deriveEncoder[Target]

  def asJson(target: Target): String = Json.asJson(target)

  def fromJson(json: String): Option[Target] = Json.fromJson(json)
}