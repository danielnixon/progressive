package org.danielnixon.progressive.play

import org.danielnixon.progressive.shared.api.AjaxResponse
import play.api.libs.json.{ Json, OWrites }

object JsonWrites {
  implicit val ajaxResponseWrites: OWrites[AjaxResponse] = Json.writes[AjaxResponse]
}
