package controllers

import play.api.mvc.{Controller, Action}
import play.api.libs.ws.WS
import scala.util.{Try, Failure, Success}
import scala.concurrent.Future
import play.api.libs.json.{Json, JsValue}


object Twitter extends Controller {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global


  def getAPIResult(token: String, apiCall: String):Future[Try[JsValue]] = {
    WS.url(apiCall)
      .withHeaders("Authorization" -> ("Bearer " + token))
      .get().map(response =>
      response.status match {
        case 200 => Success(Json.parse(response.body))
        case _ => Failure(new RuntimeException("Web service call failed: " + response.body))
      })
  }

  def getTwitterLists(token: String, username: String):Future[Try[JsValue]] = {
    getAPIResult(token, "https://api.twitter.com/1.1/lists/list.json?screen_name=" + username)
  }

  def getTwitterSubscriptions(token: String, username: String):Future[Try[JsValue]] = {
    getAPIResult(token, "https://api.twitter.com/1.1/lists/subscriptions.json?cursor=-1&screen_name=" + username)
  }


  def getAllLists(username: String) = Action.async { implicit request =>
    for {
      accessToken <- TwitterOAuth.getAccessToken
      lists <- getTwitterLists(accessToken, username)
      subscriptions <- getTwitterSubscriptions(accessToken, username)
    } yield {
      lists match {
        case Success(str) => Ok(str)
        case Failure(t)   => InternalServerError(t.getMessage)
      }
    }
  }


  def getIntersection(listId: String) = play.mvc.Results.TODO
}
