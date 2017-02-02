package org.danielnixon.progressive

import scalatags.Text.TypedTag
import scalatags.Text.all._

/**
  * Views required by Progressive. You'll need to provide an implementation of this trait when initializing Progressive.
  */
trait Views {
  /**
    * The loading view.
    * @param message The loading message to display to the user.
    * @return The loading view.
    */
  def loading(message: String): TypedTag[String]

  /**
    * The error view. This view will be displayed in the target defined by an ajax form or link.
    * @param html The error markup to display to the user.
    * @return The error view.
    */
  def error(html: Frag): TypedTag[String]

  /**
    * The global error view. This view will be displayed in the error element defined in [[KeyElements]] when an
    * ajax form or link does not define a target.
    * @param html The error markup to display to the user.
    * @return The global error view.
    */
  def globalError(html: Frag): TypedTag[String]
}
