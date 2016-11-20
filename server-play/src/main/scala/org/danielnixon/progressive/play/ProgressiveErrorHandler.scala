package org.danielnixon.progressive.play

import play.api.Logger
import play.api.http.HttpErrorHandler
import play.api.http.HttpVerbs.POST
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.mvc.Results._
import play.api.mvc.{ RequestHeader, Result }
import org.danielnixon.progressive.play.extensions.{ RequestHeaderWrapper, ResultWrapper }
import org.danielnixon.progressive.shared.api.AjaxResponse
import play.twirl.api.HtmlFormat

import scalaz.Scalaz._
import scala.concurrent.Future

/**
  * An HttpErrorHandler that responds with an AjaxResponse for ajax requests and a full
  * server-rendered page for non-ajax requests.
  * @see https://www.playframework.com/documentation/2.5.x/ScalaErrorHandling
  */
abstract class ProgressiveErrorHandler extends HttpErrorHandler {

  /**
    * A full server-rendered page to display when an error occurs.
    */
  def errorPage(request: RequestHeader, statusCode: Int, errorMessage: String): HtmlFormat.Appendable

  def clientErrorToErrorMessage(request: RequestHeader, statusCode: Int, message: String): String = {
    Option(message).filter(_.nonEmpty).getOrElse(statusCode.toString)
  }

  def serverErrorToErrorMessage(request: RequestHeader, exception: Throwable): String = {
    exception.getMessage
  }

  /**
    * Invoked when a client error occurs, that is, an error in the 4xx series.
    *
    * @param request The request that caused the client error.
    * @param statusCode The error status code.  Must be greater or equal to 400, and less than 500.
    * @param message The error message.
    */
  def onClientError(request: RequestHeader, statusCode: Int, message: String = ""): Future[Result] = {
    val errorMessage = clientErrorToErrorMessage(request, statusCode, message)

    val response = Status(statusCode)

    Future.successful {
      if (request.isAjax) response(ajaxResponse(errorMessage)) else response(errorPage(request, statusCode, errorMessage))
    }
  }

  /**
    * Invoked when a server error occurs.
    *
    * @param request The request that triggered the server error.
    * @param exception The server error.
    */
  def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    val errorMessage = serverErrorToErrorMessage(request, exception)

    Logger.error(errorMessage, exception)

    Future.successful {
      request.isAjax match {
        case true => InternalServerError(ajaxResponse(errorMessage))
        case false if request.method === POST => Redirect(request.referer.getOrElse("/")).flashingError(errorMessage)
        case _ => InternalServerError(errorPage(request, INTERNAL_SERVER_ERROR, errorMessage))
      }
    }
  }

  private def ajaxResponse(errorMessage: String): String = {
    AjaxResponse.asJson(AjaxResponse(Some(errorMessage), None))
  }
}
