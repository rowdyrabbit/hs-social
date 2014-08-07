package controllers

import play.api.mvc.{RequestHeader, Action, Controller}
import play.api.libs.oauth.{RequestToken, ServiceInfo, OAuth, ConsumerKey}
import play.Play
import play.api.libs.ws.WS
import scala.util.{Failure, Success, Try}
import scala.concurrent.Future
import org.apache.commons.codec.binary.Base64
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.oauth.ServiceInfo
import play.api.libs.oauth.OAuth
import play.api.libs.oauth.RequestToken
import scala.util.Success
import play.api.libs.oauth.ConsumerKey
import scala.util.Failure


object TwitterOAuth extends Controller {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global
  private final val consumerKey: String = Play.application.configuration.getString("securesocial.twitter.consumerKey")
  private final val consumerSecret: String = Play.application.configuration.getString("securesocial.twitter.consumerSecret")
  private final val requestTokenUrl: String = Play.application.configuration.getString("securesocial.twitter.requestTokenUrl")
  private final val accessTokenUrl: String = Play.application.configuration.getString("securesocial.twitter.accessTokenUrl")
  private final val authorizationUrl: String = Play.application.configuration.getString("securesocial.twitter.authorizationUrl")


  val KEY = ConsumerKey(consumerKey, consumerSecret)

  val TWITTER = OAuth(ServiceInfo(
    requestTokenUrl,
    accessTokenUrl,
    authorizationUrl, KEY),
    true)

  def getAccessToken(implicit request: RequestHeader): Future[String] = {
    request.session.get("accessToken") match {
      case Some(t) => Future(t)
      case None => {
        val freshFutureToken:Future[Try[String]] = TwitterOAuth.getApplicationAccessToken // this call returns a future
        freshFutureToken.map {
          case Success(token) => token
          case Failure(ex) => throw ex
        }
      }
    }
  }

  def getApplicationAccessToken(implicit request: RequestHeader):Future[Try[String]]  = {

    val encodedKey = java.net.URLEncoder.encode(consumerKey)
    val encodedSecret = java.net.URLEncoder.encode(consumerSecret)
    val credentials = encodedKey + ":" + encodedSecret
    val b64 = Base64.encodeBase64(credentials.getBytes("utf-8"))
    WS.url("https://api.twitter.com/oauth2/token")
      .withHeaders("Content-Type" -> "application/x-www-form-urlencoded;charset=UTF-8", "Authorization" -> ("Basic   ".concat(new String(b64))))
      .post("grant_type=client_credentials").map(response =>
      response.status match {
        case 200 => {
          val json: JsValue = Json.parse(response.body)
          val tokenType = (json \ "token_type").as[JsString].value
          val accessToken = (json \ "access_token").as[JsString].value
          println("token type: " + tokenType + " access token: " + accessToken)
          request.session + ("accessToken" -> accessToken)
          Success(accessToken)
        }
        case _   => println("failed: " + response.status + " " + response.body);Failure(new RuntimeException(response.body))
      })
  }

  def authenticate = Action { request =>
    request.getQueryString("oauth_verifier").map { verifier =>
      val tokenPair = sessionTokenPair(request).get
      // We got the verifier; now get the access token, store it and back to index
      TWITTER.retrieveAccessToken(tokenPair, verifier) match {
        case Right(t) => {
          // We received the authorized tokens in the OAuth object - store it before we proceed
          Redirect(routes.Application.index).withSession("token" -> t.token, "secret" -> t.secret)
        }
        case Left(e) => throw e
      }
    }.getOrElse(
        TWITTER.retrieveRequestToken("http://localhost:9000/auth") match {
          case Right(t) => {
            // We received the unauthorized tokens in the OAuth object - store it before we proceed
            Redirect(TWITTER.redirectUrl(t.token)).withSession("token" -> t.token, "secret" -> t.secret)
          }
          case Left(e) => throw e
        })
  }

    def sessionTokenPair(implicit request: RequestHeader): Option[RequestToken] = {
    for {
      token <- request.session.get("token")
      secret <- request.session.get("secret")
    } yield {
      RequestToken(token, secret)
    }
  }

}
