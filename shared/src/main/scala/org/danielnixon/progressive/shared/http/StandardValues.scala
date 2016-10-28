package org.danielnixon.progressive.shared.http

object HeaderNames extends HeaderNames

trait HeaderNames {
  val CONTENT_TYPE = "Content-Type"
  val X_REQUESTED_WITH = "X-Requested-With"
}

object MimeTypes extends MimeTypes

trait MimeTypes {
  val FORM = "application/x-www-form-urlencoded"

  val FORM_DATA = "multipart/form-data"
}

object HeaderValues extends HeaderValues

trait HeaderValues {
  val XML_HTTP_REQUEST = "XMLHttpRequest"
}