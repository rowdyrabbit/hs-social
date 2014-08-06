package services

import play.api.libs.ws.{Response, WS}
import scala.concurrent.Future
import play.api.libs.ws
import org.jsoup.nodes.{Element, Document}
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import scala.collection.mutable
import play.api.libs.json.{JsValue, Writes, Json}


object HackerSchoolOAuthAPI {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  def getAllTwitterAccounts(): Future[List[Account]] = {
    //GET /api/v1/batches/:batch_id/people
    return null

  }

  class Account(val firstName: String, val lastName: String, val twitterHandle: String) {

  }



}
