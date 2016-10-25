package org.danielnixon.progressive

import org.querki.jquery.JQuery
import org.scalajs.dom.Element

final case class KeyElements(
  body: Element,
  mainElement: JQuery,
  announcementsElement: JQuery,
  errorElement: JQuery
)
