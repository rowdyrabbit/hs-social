package services

import play.api.mvc.{Controller, Action}
import play.api.libs.ws.WS
import scala.util.{Try, Failure, Success}
import scala.concurrent.Future
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.util.Success
import scala.util.Failure


object TwitterAPI {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

//  object GroupType extends Enumeration {
//    type GroupType = Value
//    val Subscription, TwitterList = Value
//  }
  case class TwitterGroup(id: String, name: String)

  implicit val twitterGroupReads: Reads[TwitterGroup] = (
    (JsPath \ "id_str").read[String] and
    (JsPath \ "name").read[String]
    )(TwitterGroup.apply _)


  def getAPIResult(token: String, apiCall: String):Future[Try[JsValue]] = {
    WS.url(apiCall)
      .withHeaders("Authorization" -> ("Bearer " + token))
      .get().map(response =>
      response.status match {
        case 200 => Success(Json.parse(response.body))
        case _ => Failure(new RuntimeException("Web service call failed: " + response.body))
      })
  }

//  def convertFromJsValueToObject(json: JsValue): Try[TwitterGroup] = {
//    json.validate[TwitterGroup] match {
//      case s: JsSuccess[TwitterGroup] => {
//        Success(s.get)
//      }
//      case e: JsError => {
//        Failure(new RuntimeException("Could not convert JsValue to object"))
//      }
//    }
//  }

  def convertFromJsValueToObject(json: Try[JsValue]): Try[TwitterGroup] = {
    json match {
      case Success(v) => {
        v.validate[TwitterGroup] match {
          case s: JsSuccess[TwitterGroup] => {
            Success(s.get)
          }
          case e: JsError => {
            Failure(new RuntimeException("Could not convert JsValue to object"))
          }
        }
      }
      case Failure(t) => Failure(t)
    }

  }

  def getTwitterLists(token: String, username: String):Future[Try[TwitterGroup]] = {
    val result:Future[Try[JsValue]] = getAPIResult(token, "https://api.twitter.com/1.1/lists/list.json?screen_name=" + username)

    result.map(x => convertFromJsValueToObject(x))
//
//    for {
//      result <- getAPIResult(token, "https://api.twitter.com/1.1/lists/list.json?screen_name=" + username)
//      objResult <- convertFromJsValueToObject(result)
//    } yield objResult

  }

  def getTwitterSubscriptions(token: String, username: String):Future[Try[JsValue]] = {
    getAPIResult(token, "https://api.twitter.com/1.1/lists/subscriptions.json?cursor=-1&screen_name=" + username)
  }


  def getIntersection(listId: String) = play.mvc.Results.TODO
}
