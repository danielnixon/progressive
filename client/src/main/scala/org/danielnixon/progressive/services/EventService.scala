package org.danielnixon.progressive.services

import org.danielnixon.saferdom._

import scalaz._
import Scalaz._

class EventService {
  def shouldHijackLinkClick(e: MouseEvent): Boolean = !(e.shiftKey || e.ctrlKey || e.metaKey || e.button === 1)
}
