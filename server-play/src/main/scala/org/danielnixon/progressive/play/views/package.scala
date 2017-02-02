package org.danielnixon.progressive.play

import org.danielnixon.progressive.shared.api.{ FormSettings, LinkSettings, RefreshSettings, SubmitButtonSettings }
import org.danielnixon.progressive.{ views => baseViews }
import play.api.mvc.Call
import play.twirl.api.Html

import scalatags.Text.TypedTag
import scalatags.Text.all._

import scala.language.implicitConversions

/**
  * Twirl wrappers around ScalaTags views.
  */
package object views {
  def progressiveForm(action: Call, settings: FormSettings, attributes: Seq[(String, String)] = Nil)(html: Html): Html = {
    val baseView = baseViews.progressiveForm(action.method, action.url, settings)(raw(html.body))
    applyAttributes(baseView, attributes)
  }

  def progressiveSubmitButton(action: Call, settings: SubmitButtonSettings)(html: Html): Html = {
    applyAttributes(baseViews.progressiveSubmitButton(action.method, action.url, settings)(raw(html.body)))
  }

  def progressiveLink(action: Call, settings: LinkSettings)(html: Html): Html = {
    applyAttributes(baseViews.progressiveLink(action.toString, settings)(raw(html.body)))
  }

  def refresh(url: Call, interval: Option[Int] = None, attributes: Seq[(String, String)] = Nil)(html: Html): Html = {
    val baseView = baseViews.refresh(RefreshSettings(url.toString, interval))(raw(html.body))
    applyAttributes(baseView, attributes)
  }

  def refreshContent(html: Html): Html = {
    applyAttributes(baseViews.refreshContent(raw(html.body)))
  }

  def progressiveTarget: Html = {
    applyAttributes(baseViews.progressiveTarget)
  }

  private def applyAttributes(view: TypedTag[String], attributes: Seq[(String, String)] = Nil): TypedTag[String] = {
    val attrs = attributes.map({ case (k, v) => attr(k) := v })
    attrs.foldLeft(view) { (v, attr) =>
      v(attr)
    }
  }

  /**
    * Implicit conversion from a ScalaTags Frag to a Twirl Html.
    */
  implicit def fragToHtml(frag: Frag): Html = play.twirl.api.Html(frag.render)

  /**
    * Implicit conversion from a Twirl Html to a ScalaTags Frag.
    */
  implicit def htmlToFrag(html: Html): RawFrag = raw(html.body)
}
