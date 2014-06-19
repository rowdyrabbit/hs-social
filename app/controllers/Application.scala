package controllers

import play.api.mvc._
import services.HackerSchoolAPI
import play.api.libs.ws.Response
import scala.concurrent.Future
import play.api.libs.json.{JsString, Json, JsValue}

object Application extends Controller {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  def index = Action {
    Ok(views.html.index())
  }

  //  def getAllTwitterAccounts() = Action.async {
  //    val username = Play.application().configuration().getString("hs.username")
  //    val password = Play.application().configuration().getString("hs.password")
  //
  //    retrieveAllTwitterAccounts(username, password)
  //  }

  

  def login = Action.async { implicit request =>

    val body = request.body
    val json: JsValue = body.asJson.get

    val username = (json \ "username").as[JsString].value
    val password = (json \ "password").as[JsString].value

    retrieveAllTwitterAccounts(username, password)

    //I would prefer to do the following so I can handle errors but don't know how to deal with the different types :/
    //    hsLoginForm.bindFromRequest.fold(
    //      formWithErrors => BadRequest("Error logging in"),
    //      login => Redirect(routes.Application.getAllTwitterAccounts())
    //    )
  }

  def retrieveAllTwitterAccounts(username: String, password: String): Future[SimpleResult] = {
    val hackerSchoolAccounts: Future[Response] = HackerSchoolAPI.scrapeAllTwitterAccounts(username, password)

    val result: Future[SimpleResult] = hackerSchoolAccounts.map(response => Ok(HackerSchoolAPI.generateJSONResponse(response)).as("application/json"))
    result
  }

  def preflight(all: String) = Action {
    Ok("").withHeaders("Access-Control-Allow-Origin" -> "*",
      "Allow" -> "*",
      "Access-Control-Allow-Methods" -> "POST, GET, PUT, DELETE, OPTIONS",
      "Access-Control-Allow-Headers" -> "Origin, X-Requested-With, Content-Type, Accept, Referrer, User-Agent");
  }

}