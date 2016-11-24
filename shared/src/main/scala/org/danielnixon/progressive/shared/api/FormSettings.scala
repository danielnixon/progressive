package org.danielnixon.progressive.shared.api

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import org.danielnixon.progressive.shared.Wart

final case class FormSettings(
  ajax: Boolean = true,
  target: Option[Target] = None,
  refreshTarget: Option[Target] = Some(Target.ClosestRefresh),
  busyMessage: Option[String] = None,
  focusTarget: Boolean = true,
  reloadPage: Boolean = false,
  confirmMessage: Option[String] = None,
  confirmedAction: Option[String] = None,
  ajaxAction: Option[String] = None,
  remove: Boolean = false
)

@SuppressWarnings(Array(Wart.AsInstanceOf, Wart.Nothing))
object FormSettings {
  implicit val decoder: Decoder[FormSettings] = deriveDecoder[FormSettings]
  implicit val encoder: Encoder[FormSettings] = deriveEncoder[FormSettings]

  def asJson(target: FormSettings): String = Json.asJson(target)

  def fromJson(json: String): Option[FormSettings] = Json.fromJson(json)
}