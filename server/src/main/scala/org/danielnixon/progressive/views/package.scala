package org.danielnixon.progressive

import org.danielnixon.progressive.shared.api.{ FormSettings, LinkSettings, RefreshSettings, SubmitButtonSettings }

import scalatags.Text.TypedTag
import scalatags.Text.all._

package object views {
  def progressiveForm(formMethod: String, formAction: String, settings: FormSettings): TypedTag[String] = {
    form(
      method := formMethod,
      action := formAction,
      data.progressive := FormSettings.asJson(settings)
    )
  }

  def progressiveSubmitButton(formMethod: String, formAction: String, settings: SubmitButtonSettings): TypedTag[String] = {
    button(
      `type` := "submit",
      formmethod := formMethod,
      formaction := formAction,
      data.progressive := SubmitButtonSettings.asJson(settings)
    )
  }

  def progressiveLink(linkHref: String, settings: LinkSettings): TypedTag[String] = {
    a(
      href := linkHref,
      data.progressive := LinkSettings.asJson(settings)
    )
  }

  def refresh(settings: RefreshSettings): TypedTag[String] = {
    div(
      data.refresh := RefreshSettings.asJson(settings)
    )
  }
}
