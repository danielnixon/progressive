package org.danielnixon.progressive.shared.http

object HeaderNames extends HeaderNames

trait HeaderNames {
  val CONTENT_TYPE: String = "Content-Type"
  val X_REQUESTED_WITH: String = "X-Requested-With"
}

object MimeTypes extends MimeTypes

trait MimeTypes {
  val FORM: String = "application/x-www-form-urlencoded"
  val FORM_DATA: String = "multipart/form-data"
}

object HeaderValues extends HeaderValues

trait HeaderValues {
  val XML_HTTP_REQUEST: String = "XMLHttpRequest"
}