package org.danielnixon.progressive.extensions

import org.danielnixon.progressive.facades.virtualdom.PatchObject

import scala.scalajs.js
import scalaz._
import Scalaz._

package object virtualdom {
  implicit class PatchObjectWrapper(private val patchObject: PatchObject) extends AnyVal {
    def isEmpty = js.Object.keys(patchObject).length lte 1
  }
}
