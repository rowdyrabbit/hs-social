package services

import play.api.libs.ws.{Response, WS}
import scala.concurrent.Future
import play.api.libs.ws
import org.jsoup.nodes.{Element, Document}
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import scala.collection.mutable
import play.api.libs.json.{JsValue, Writes, Json}


object HackerSchoolAPI {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  implicit val batchlingWrites = new Writes[Batchling] {
    def writes(batchling: Batchling) = Json.obj(
      "name" -> batchling.name,
      "twitter" -> batchling.twitterHandle
    )
  }

  implicit val batchWrites = new Writes[Batch] {
    def writes(batch: Batch) = Json.obj(
      "id" -> batch.id,
      "name" -> batch.name,
      "batchlings" -> batch.batchlings
    )
  }

  def extractAuthToken(response: ws.Response): String = {
    val body = response.body;
    val doc = Jsoup.parse(body);
    doc.select("[name=authenticity_token]").first().`val`()
  }


  def scrapeAllTwitterAccounts(username: String, password: String) : Future[Response] = {

    val response: Future[Response] = for {
      loginPageResp <- WS.url("https://www.hackerschool.com/login").get()
      loginPostResp <- WS.url("https://www.hackerschool.com/sessions")
        .withHeaders("Content-Type" -> "application/x-www-form-urlencoded",
          "User-Agent" -> "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36",
          "Cookie" -> loginPageResp.cookies.toString()
        )
        .post("utf8=%E2%9C%93&authenticity_token=" + extractAuthToken(loginPageResp) + "&email=" + username + "&password=" + password + "&commit=Log+in")

    } yield loginPostResp
    response
  }

  def generateJSONResponse(resp: Response) : JsValue = {
    //take the response and map it to an object list via JSoup
    var batchList: List[Batch] = List[Batch]()
    val document : Document = Jsoup.parse(resp.body)
    val batchContent = document.getElementById("batches")
    import scala.collection.JavaConversions._
    val allBatches = batchContent.children().select("ul")
    for (batch <- allBatches) {
      val b : Batch = buildBatch(batch)
      batchList = batchList :+ b
    }
    //then convert to JSON and return it
    val json = Json.toJson(batchList)
    json
  }

  def buildBatch(batchElement: Element): Batch = {
    val batchName :String = batchElement.attr("id");
    val batch : Batch = new Batch(batchName, batchName);
    import scala.collection.JavaConversions._
    val batchlings : Elements = batchElement.getElementsByTag("li");
    for (currPerson: Element <- batchlings) {
      batch.addBatchling(currPerson.select("div.name > a").text(), getTwitterHandle(currPerson));
    }
    return batch
  }

  def getTwitterHandle(batchling : Element) : String =  {
    val twitterLink : Element = batchling.select("[href*=twitter.com]").first();
    if (twitterLink != null) {
      val twitterUrl : String = twitterLink.attr("href");
      val twitterHandle : String = twitterUrl.substring(twitterUrl.lastIndexOf("/") + 1, twitterUrl.length());
      return twitterHandle;
    } else {
      return null;
    }
  }

  class Batchling(val name: String, val twitterHandle: String) {
    override def toString = {
      name + " - " + twitterHandle
    }
  }

  class Batch(val id: String, val name: String) {

    var batchlings: mutable.LinkedList[Batchling] = mutable.LinkedList[Batchling]()

    def addBatchling(name: String, twitterHandle: String) {
      batchlings = batchlings :+ new Batchling(name, twitterHandle)
    }

    override def toString = {
      val sb : StringBuilder = new StringBuilder
      sb.append("Batch id: " + id + "\n")
      for (person <- batchlings) {
        sb.append(person + "\n")
      }
      sb.toString()
    }

  }

}
