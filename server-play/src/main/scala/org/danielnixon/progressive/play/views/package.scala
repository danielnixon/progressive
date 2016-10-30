package org.danielnixon.progressive.play

import org.danielnixon.progressive.shared.api.{ FormSettings, RefreshSettings, SubmitButtonSettings }
import org.danielnixon.progressive.{ views => baseViews }
import play.api.mvc.Call
import play.twirl.api.Html

import scalatags.Text.TypedTag
import scalatags.Text.all._

/**
  * Twirl wrappers around scalatags views.
  */
package object views {
  def progressiveForm(action: Call, settings: FormSettings, attributes: Seq[(String, String)] = Nil)(html: Html): Html = {
    val baseView = baseViews.progressiveForm(action.method, action.url, settings)(raw(html.body))
    applyAttributes(baseView, attributes)
  }

  def progressiveSubmitButton(action: Call, settings: SubmitButtonSettings)(html: Html): Html = {
    applyAttributes(baseViews.progressiveSubmitButton(action.method, action.url, settings)(raw(html.body)))
  }

  def refresh(url: Call, interval: Option[Int] = None, attributes: Seq[(String, String)] = Nil)(html: Html): Html = {
    val baseView = baseViews.refresh(RefreshSettings(url.toString, interval))(raw(html.body))
    applyAttributes(baseView, attributes)
  }

  private def applyAttributes(view: TypedTag[String], attributes: Seq[(String, String)] = Nil): Html = {
    val attrs = attributes.map({ case (k, v) => attr(k) := v })
    val viewWithAttrs = attrs.foldLeft(view) { (v, attr) =>
      v(attr)
    }
    Html(viewWithAttrs.render)
  }
}
