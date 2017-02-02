package org.danielnixon.progressive.play.views

/**
  * Based on play.twirl.api.Html but overloads apply to also support ScalaTags.
  */
object Html {
  /**
    * Creates an HTML fragment with initial content specified.
    */
  def apply(text: String): play.twirl.api.Html = play.twirl.api.Html(text)

  /**
    * Creates an HTML fragment with initial content specified.
    */
  def apply[T](tag: T)(implicit ev: T => play.twirl.api.Html): play.twirl.api.Html = ev(tag)
}
