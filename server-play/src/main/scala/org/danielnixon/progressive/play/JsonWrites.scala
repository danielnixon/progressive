package org.danielnixon.progressive.play

import org.danielnixon.progressive.shared.Wart
import org.danielnixon.progressive.shared.api.AjaxResponse
import play.api.libs.json.{ Json, OWrites }

object JsonWrites {
  @SuppressWarnings(Array(Wart.Nothing))
  implicit val ajaxResponseWrites: OWrites[AjaxResponse] = Json.writes[AjaxResponse]
}
