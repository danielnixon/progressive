package org.danielnixon.progressive.shared.api

import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import io.circe.{ Decoder, Encoder }
import org.danielnixon.progressive.shared.Wart

object Json {
  @SuppressWarnings(Array(Wart.AsInstanceOf))
  def asJson[A](t: A)(implicit e: Encoder[A]): String = t.asJson.noSpaces

  @SuppressWarnings(Array(Wart.AsInstanceOf))
  def fromJson[A](json: String)(implicit d: Decoder[A]): Option[A] = decode[A](json).toOption
}
