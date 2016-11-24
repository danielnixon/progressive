package org.danielnixon.progressive.shared.api

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import org.danielnixon.progressive.shared.Wart

/**
  * The target of an ajax form or link.
  */
sealed trait Target

@SuppressWarnings(Array(Wart.AsInstanceOf, Wart.Nothing))
object Target {

  /**
    * The next sibling element node.
    */
  case object Next extends Target

  /**
    * The direct parent element node.
    */
  case object Parent extends Target

  /**
    * The first descendant element node with the target CSS class.
    */
  case object ChildTarget extends Target

  /**
    * The closest ancestor element node with the refresh data attribute.
    */
  case object ClosestRefresh extends Target

  implicit val decoder: Decoder[Target] = deriveDecoder[Target]
  implicit val encoder: Encoder[Target] = deriveEncoder[Target]

  def asJson(target: Target): String = Json.asJson(target)

  def fromJson(json: String): Option[Target] = Json.fromJson(json)
}