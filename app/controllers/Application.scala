package controllers

import play.api._
import play.api.mvc._
import services.HackerSchoolAPI
import play.api.libs.ws.Response
import scala.concurrent.Future

object Application extends Controller {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def getAllTwitterAccounts = Action.async {

    val hackerSchoolAccounts: Future[Response] = HackerSchoolAPI.scrapeAllTwitterAccounts()

    hackerSchoolAccounts.map(response => Ok("WOOOOO " + HackerSchoolAPI.generateJSONResponse(response) ))

  }

}