package controllers

import play.api.libs.ws.{Response, WS}
import scala.concurrent.{Future}
import play.api.mvc._
import scala.concurrent.Future
import play.Play
import play.api.libs.json.{JsString, Json, JsValue}


object HackerSchool extends Controller {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global
  private final val consumerKey: String = Play.application.configuration.getString("securesocial.hackerschool.consumerKey")
  private final val consumerSecret: String = Play.application.configuration.getString("securesocial.hackerschool.consumerSecret")
  private final val accessTokenUrl: String = Play.application.configuration.getString("securesocial.hackerschool.accessTokenUrl")
  private final val authorizationUrl: String = Play.application.configuration.getString("securesocial.hackerschool.authorizationUrl")


  def getAuthorizationUrl() = Action { implicit request =>
    val url = authorizationUrl + "?client_id=" + consumerKey + "&response_type=code" + "&redirect_uri=http://localhost:9002/oauth-redirect.html"
    Ok(Json.obj("authUrl" -> url))
  }


  def getAllBatches(accessCode: String) = Action.async { implicit request =>
    WS.url("https://www.hackerschool.com/api/v1/batches")
      .withHeaders("Authorization" -> ("Bearer " + accessCode))
      .get().map(response =>
        response.status match {
          case 200 => Ok(response.body)
          case 401 => Unauthorized(response.body) //need to handle the case where auth fails here, and send the error response
          case _   => InternalServerError(response.body)
        })

    }


  def getAccessToken(clientCode: String) = Action.async { implicit request =>
    authenticate(clientCode).map { token =>
      Ok(Json.obj("access_token" -> Json.parse(token.body)))
    }
  }


  private def authenticate(code: String): Future[Response] = {

    val url = accessTokenUrl //+ "?client_id=" + consumerKey + "&client_secret=" + consumerSecret + "&code=" + code + "&grant_type=authorization_code"
    val postBody = "client_id=" + consumerKey + "&client_secret=" + consumerSecret + "&code=" + code + "&grant_type=authorization_code&redirect_uri=http://localhost:9002/oauth-redirect.html"
    WS.url(url).withHeaders("Content-Type" -> "application/x-www-form-urlencoded").post(postBody)
  }



}
