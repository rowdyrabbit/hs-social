package controllers

import play.api.mvc.Controller
import securesocial.core._



object Twitter extends Controller with securesocial.core.SecureSocial {

  def onlyTwitter = SecuredAction(WithProvider("twitter")) {
    Ok("You can see this because you logged in using Twitter")
    // do something here
  }

}

case class WithProvider(provider: String) extends Authorization {
  def isAuthorized(user: Identity) = {
    user.identityId.providerId == provider
  }
}
