package org.danielnixon.progressive.shared.api

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.deriveFor
import org.danielnixon.progressive.shared.Wart

final case class FormSettings(
  ajax: Boolean = true,
  target: Option[Target] = None,
  triggerRefresh: Boolean = true,
  busyMessage: Option[String] = None,
  focusTarget: Boolean = true,
  reloadPage: Boolean = false,
  confirmMessage: Option[String] = None,
  confirmedAction: Option[String] = None,
  ajaxAction: Option[String] = None,
  remove: Boolean = false
)

@SuppressWarnings(Array(Wart.AsInstanceOf))
object FormSettings {
  implicit val decoder: Decoder[FormSettings] = deriveFor[FormSettings].decoder
  implicit val encoder: Encoder[FormSettings] = deriveFor[FormSettings].encoder

  def asJson(target: FormSettings): String = Json.asJson(target)

  def fromJson(json: String): Option[FormSettings] = Json.fromJson(json)
}