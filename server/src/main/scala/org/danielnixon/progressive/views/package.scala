package org.danielnixon.progressive

import org.danielnixon.progressive.shared.api._

import scalatags.Text.all._

/**
  * ScalaTags views for Progressive forms, links, etc.
  */
package object views {
  def progressiveForm(formMethod: String, formAction: String, settings: FormSettings): Tag = {
    form(
      method := formMethod,
      action := formAction,
      attr(DataAttributes.progressive) := FormSettings.asJson(settings)
    )
  }

  def progressiveSubmitButton(formMethod: String, formAction: String, settings: SubmitButtonSettings): Tag = {
    button(
      `type` := "submit",
      formmethod := formMethod,
      formaction := formAction,
      attr(DataAttributes.progressive) := SubmitButtonSettings.asJson(settings)
    )
  }

  def progressiveLink(linkHref: String, settings: LinkSettings): Tag = {
    a(
      href := linkHref,
      attr(DataAttributes.progressive) := LinkSettings.asJson(settings)
    )
  }

  def refresh(settings: RefreshSettings): Tag = {
    div(attr(DataAttributes.refresh) := RefreshSettings.asJson(settings))
  }

  def refreshContent: Tag = {
    div(cls := CssClasses.refreshContent)
  }

  def progressiveTarget: Tag = {
    div(cls := CssClasses.target)
  }
}
