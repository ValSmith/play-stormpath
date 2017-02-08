package controllers

import authentication.AuthenticationGlobal._
import authentication.{AuthenticatedAction, StormpathAuthenticationService}
import com.stormpath.sdk.account.Account
import com.stormpath.sdk.group.Groups
import com.stormpath.sdk.oauth.OAuthGrantRequestAuthenticationResult
import com.stormpath.sdk.resource.ResourceException
import model.AccountFields
import play.api.mvc._

import scala.io.Source

/**
  * Created by Val Smith on 10/18/16.
  * This class is the implementation of the required api endpoints for the Stormpath AngularJS SDK
  * For details of the endpoints see the page
  * https://docs.stormpath.com/angularjs/sdk/#/api/stormpath.STORMPATH_CONFIG:STORMPATH_CONFIG
  */
class Authentication extends Controller{

  def getMe: Action[AnyContent] = AuthenticatedAction{ (request, account) =>
    Results.Ok(AccountFields.toJson(account)).as("application/json")
  }

  def loginUser() = Action { request =>
    val form = request.body.asFormUrlEncoded.get
    val user = form.get("login").flatMap(_.headOption)
    val password = form.get("password").flatMap(_.headOption)

    if (user.isDefined && password.isDefined) {
      val result = StormpathAuthenticationService.verifyUserPassword(user.get, password.get)

      result.map(r => createAuthenticatedResponse(r._1, r._2)).getOrElse(Results.Unauthorized)
    } else {
      Results.Unauthorized
    }
  }

  def createAuthenticatedResponse(result: OAuthGrantRequestAuthenticationResult, account: Account): Result = {
    Results.Ok(AccountFields.toJson(account)).as("application/json")
      .withCookies(StormpathAuthenticationService.createCookiesFromResult(result):_*)
  }

  def loginFormData() = Action {
    readJsonResource("LoginForm.json")
  }

  def logout() = Action{
    Results.Ok.discardingCookies(DiscardingCookie(AccessTokenName), DiscardingCookie(RefreshTokenName))
  }

  def registerFormData() = Action {
    readJsonResource("RegisterForm.json")
  }

  private def readJsonResource(fileName: String) = {
    Results.Ok(Source.fromInputStream(getClass.getClassLoader.getResourceAsStream(fileName)).mkString)
      .as("application/json")
  }

  def registerUser() = Action { request =>
    val account = client.instantiate(classOf[Account])
    val form = request.body.asFormUrlEncoded.get
    account.setGivenName(form("givenName").head)
    account.setSurname(form("surname").head)
    account.setEmail(form("email").head)
    account.setPassword(form("password").head)

    val instantiatedAccount = application.createAccount(account)
    val group = findGroup(form("group").head)
    group.foreach(g => {
      g.addAccount(instantiatedAccount)
      g.save()
    })

    Results.Ok
  }

  private def findGroup(name: String) = {
    try {
      Some(directory.getGroups(Groups.where(Groups.name().eqIgnoreCase(name))).single())
    } catch {
      case e: IllegalStateException => None
    }
  }

  def verifyNewAccountEmail() = Action { request =>
    try {
      request.getQueryString("sptoken")
        .map(client.verifyAccountEmail)
        .map(a => Results.Ok)
        .getOrElse(Results.BadRequest)
    } catch {
      case e: ResourceException =>
        Results.Unauthorized
    }
  }

  def forgotPassword() = Action { request =>
    try {
      request.body.asFormUrlEncoded.get("email").headOption
        .map(application.sendPasswordResetEmail)
        .map(a => Results.Ok)
        .getOrElse(Results.BadRequest)
    } catch {
      case e: ResourceException => Results.BadRequest
    }
  }

  def verifyPasswordResetToken() = Action { request =>
    try {
      request.getQueryString("sptoken")
        .map(application.verifyPasswordResetToken)
        .map(a => Results.Ok)
        .getOrElse(Results.BadRequest)
    } catch {
      case e: ResourceException =>
        Results.Unauthorized
    }
  }

  def changePassword() = Action{ request =>
    val data: Map[String, Seq[String]] = request.body.asFormUrlEncoded.get
    val newPassword = data.get("password").flatMap(_.headOption)
    val resetToken = data.get("sptoken").flatMap(_.headOption)
    try {
      if (newPassword.isDefined && resetToken.isDefined) {
        application.resetPassword(resetToken.get, newPassword.get)
        Results.Ok
      } else {
        Results.BadRequest
      }
    } catch {
      case e: ResourceException =>
        Results.Unauthorized
    }
  }

  def redirect(path: String) = Action { request =>
    Results.Redirect(s"/#$path", request.queryString)
  }
}
