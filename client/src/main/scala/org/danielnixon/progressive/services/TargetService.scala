package org.danielnixon.progressive.services

import org.danielnixon.progressive.extensions.dom.ElementWrapper
import org.danielnixon.progressive.shared.api.{ CssClasses, DataAttributes, Target }
import org.scalajs.dom.html
import org.danielnixon.saferdom.implicits._
import org.scalajs.dom.raw.{ Element, HTMLElement }

class TargetService {
  def getTargetElement(element: Element, target: Target): Option[html.Element] = {
    val targetElement = target match {
      case Target.Next => element.nextElementSiblingOpt
      case Target.Parent => Some(element).collect({ case e: HTMLElement => e }).flatMap(_.parentElementOpt)
      case Target.ChildTarget => element.querySelectorOpt(s".${CssClasses.target}")
      case Target.ClosestRefresh => element.closest(s"[${DataAttributes.refresh}]")
    }

    targetElement collect {
      case htmlElement: html.Element => htmlElement
    }
  }
}
