package org.danielnixon.progressive

import scalatags.Text.TypedTag

trait Views {
  def loading(message: String): TypedTag[String]

  def error(html: String): TypedTag[String]

  def globalError(html: String): TypedTag[String]
}
