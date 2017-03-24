package org.danielnixon.progressive.services

import org.danielnixon.progressive.extensions.dom.ElementWrapper
import org.danielnixon.progressive.shared.api.{ CssClasses, DataAttributes, Target }
import org.danielnixon.saferdom.html
import org.danielnixon.saferdom.implicits._
import org.danielnixon.saferdom.raw.Element

class TargetService {
  def getTargetElement(element: Element, target: Target): Option[html.Element] = {
    val targetElement = target match {
      case Target.Next => element.nextElementSibling
      case Target.Parent => element.parentElement
      case Target.ChildTarget => element.querySelector(s".${CssClasses.target}")
      case Target.ClosestRefresh => element.closest(s"[${DataAttributes.refresh}]")
    }

    targetElement collect {
      case htmlElement: html.Element => htmlElement
    }
  }
}
