package org.danielnixon.progressive.play

import play.api.Logger
import play.api.http.HttpErrorHandler
import play.api.http.HttpVerbs.{ GET, POST }
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.mvc.Results._
import play.api.mvc.{ Call, RequestHeader, Result }
import org.danielnixon.progressive.play.extensions.{ RequestHeaderWrapper, ResultWrapper }
import org.danielnixon.progressive.play.Results.redirectToRefererOrElse
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

  /**
    * The default route to use when redirecting a failed post request that did not have a referer.
    */
  def errorRoute: Call = Call(GET, "/")

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
    errorResponse(request, errorMessage, statusCode)
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
    errorResponse(request, errorMessage, INTERNAL_SERVER_ERROR)
  }

  private def errorResponse(request: RequestHeader, errorMessage: String, statusCode: Int): Future[Result] = {
    val status = Status(statusCode)

    Future.successful {
      request.isAjax match {
        case true => status(AjaxResponse.asJson(AjaxResponse(Some(errorMessage), None, None)))
        case false if request.method === POST => redirectToRefererOrElse(errorRoute)(request).flashingError(errorMessage)
        case _ => status(errorPage(request, statusCode, errorMessage))
      }
    }
  }
}
