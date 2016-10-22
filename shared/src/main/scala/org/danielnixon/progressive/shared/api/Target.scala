package org.danielnixon.progressive.shared.api

import io.circe.{ Decoder, Encoder }
import io.circe.generic.auto._
import org.danielnixon.progressive.shared.Wart

sealed trait Target {
  override def toString: String
}

object Target {

  case object Next extends Target

  case object Parent extends Target

  case object ChildTarget extends Target

  @SuppressWarnings(Array(Wart.AsInstanceOf))
  def asJson(target: Target): String = Json.asJson(target)(implicitly[Encoder[Target]])

  @SuppressWarnings(Array(Wart.AsInstanceOf))
  def fromJson(json: String): Option[Target] = Json.fromJson(json)(implicitly[Decoder[Target]])
}