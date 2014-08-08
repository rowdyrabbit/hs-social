package services

import play.api.libs.ws.WS
import scala.concurrent.Future
import play.api.libs.json._
import play.api.libs.functional.syntax._


object TwitterAPI {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global
  case class TwitterGroup(id: String, name: String)

  implicit val twitterGroupReads: Reads[TwitterGroup] = (
    (JsPath \ "id_str").read[String] and
    (JsPath \ "name").read[String]
    )(TwitterGroup.apply _)

  implicit val twitterGroupWrites: Writes[TwitterGroup] = (
    (JsPath \ "id").write[String] and
    (JsPath \ "name").write[String]
    )(unlift(TwitterGroup.unapply))


  private def getAPIResult(token: String, apiCall: String):Future[JsValue] = {
    WS.url(apiCall)
      .withHeaders("Authorization" -> ("Bearer " + token))
      .get().map(response =>
      response.status match {
        case 200 => Json.parse(response.body)
        case _ => throw new RuntimeException("Web service call failed: " + response.body)
      }
    )
  }

  private def convertFromListJsValueToObject(json: JsValue): Seq[TwitterGroup] = {
    json.as[Seq[TwitterGroup]]
  }


  def getTwitterLists(token: String, username: String):Future[Seq[TwitterGroup]] = {
    val result:Future[JsValue] = getAPIResult(token, "https://api.twitter.com/1.1/lists/list.json?screen_name=" + username)

    result.map( x => convertFromListJsValueToObject(x))
  }


  def getIntersection(listId: String) = play.mvc.Results.TODO
}
