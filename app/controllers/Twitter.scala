package controllers

import play.api.mvc.{Controller, Action}
import play.api.libs.json.Json
import services.TwitterAPI
import services.TwitterAPI.TwitterGroup


object Twitter extends Controller {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global


  def getAllLists(username: String) = Action.async { implicit request =>
    for {
      accessToken <- TwitterOAuth.getAccessToken
      lists <- TwitterAPI.getTwitterLists(accessToken, username)
    } yield {
      lists match {
        case l:Seq[TwitterGroup]=> Ok(Json.toJson(l))
        case t:Throwable => InternalServerError(t.getMessage)
      }
    }
  }

  def getIntersection(listId: String) = play.mvc.Results.TODO
}
