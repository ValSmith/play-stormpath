package authentication

import authentication.AuthenticationGlobal.{AccessTokenName, RefreshTokenName}
import authentication.StormpathAuthenticationService.{createCookiesFromResult, verifyAccessToken, verifyRefreshToken}
import com.stormpath.sdk.account.Account
import play.api.mvc._

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
/**
  * Created by vasmith on 11/12/16.
  * Class for handling secured request.  The usage mimics the play.api.mvc.Action
  */
object AuthenticatedAction {

  /**
    * Constructs an `AuthenticatedAction`
    *
    * For example:
    * {{{
    * val echo = Action(parse.anyContent) { (request, account) =>
    *   Ok("Got request [" + request + "] for [" + account + "]")
    * }
    * }}}
    *
    * @param bodyParser the `BodyParser` to use to parse the request body
    * @param authenticated the action code handling the request and authenticated account
    * @tparam T the type of the request body
    * @return an action builder
    */
  def apply[T](bodyParser: BodyParser[T])(authenticated: (Request[T], Account) => Result): AuthenticatedActionBuilder[T] =
    async(bodyParser){case (req: Request[T], act: Account) => Future.successful(authenticated(req, act))}

  /**
    * Constructs an `AuthenticatedAction` with default context
    *
    * For example:
    * {{{
    * val echo = Action { (request, account) =>
    *   Ok("Got request [" + request + "] for [" + account + "]")
    * }
    * }}}
    *
    * @param authenticated the action code handling the request and authenticated account
    * @return an action builder
    */
  def apply(authenticated: (Request[AnyContent], Account) => Result): AuthenticatedActionBuilder[AnyContent] =
    apply(BodyParsers.parse.default)(authenticated)

  /**
    * Constructs an `AuthenticatedAction` with default context and no request parameter.
    *
    * For example:
    * {{{
    * val echo = Action {
    *   Ok("Hello World!")
    * }
    * }}}
    *
    * @param authenticated the action code creating the response
    * @return an action builder
    */
  def apply(authenticated: => Result): AuthenticatedActionBuilder[AnyContent] =
    apply(BodyParsers.parse.ignore(AnyContentAsEmpty: AnyContent))((x: Request[AnyContent], Account) => authenticated)

  /**
    * The asynchronous version of the previous requests.
    *
    * For example:
    * {{{
    * val echo = Action(parse.anyContent) { (request, account) =>
    *   Future.successful(Ok("Got request [" + request + "] for [" + account + "]"))
    * }
    * }}}
    */
  def async[T](bodyParser: BodyParser[T])(authenticated: (Request[T], Account) => Future[Result]): AuthenticatedActionBuilder[T] =
    AuthenticatedActionBuilder(bodyParser, authenticated)

  def async(authenticated: (Request[AnyContent], Account) => Future[Result]): AuthenticatedActionBuilder[AnyContent] =
    async(BodyParsers.parse.default)(authenticated)

  def async(authenticated: => Future[Result]): AuthenticatedActionBuilder[AnyContent] =
    async(BodyParsers.parse.ignore(AnyContentAsEmpty: AnyContent))((x: Request[AnyContent], Account) => authenticated)

}

/**
  * The builder for adding additional behavior to authenticated requests.
  * The default behavior is to return a status code 401(unauthorized) and take no special action based on groups.
  * This class should not be initialized directly but created through the AuthenticatedAction object.
  * This class is itself an action so there is no need to convert it after creating it.
  */
case class AuthenticatedActionBuilder[T]  (private val bodyParser: BodyParser[T],
                                         private val authenticated: (Request[T], Account) => Future[Result],
                                         private val notAuthenticated: Request[T] => Future[Result] = (x: Request[T]) => Future.successful(Results.Unauthorized),
                                         private val authorizedRoles: Set[String] = Set()) extends Action[T] {

  /**
    * Methods to add different behavior for unauthorized requests.  Mixing asynchronous/syncronus results between
    * the authenticated and not authenticated actions.
    *
    * For example:
    * {{{
    * val echo = Action.async {
    *   Future.successful(Ok("Hello World!"))
    * }.notAuthenticated{ request =>
    *   Results.Redirect("/login", 200)
    * }
    * }}}
    *
    * @param asyncBlock the block to execute when the user making the request is unauthorized
    * @return A Builder with the new unauthorized behavior
    */

  def notAuthenticatedAsync(asyncBlock: Request[T] => Future[Result]): AuthenticatedActionBuilder[T] = copy(notAuthenticated = asyncBlock)
  def notAuthenticated(block: Request[T] => Result): AuthenticatedActionBuilder[T] = notAuthenticatedAsync((req: Request[T]) => Future.successful(block(req)))
  def notAuthenticated(result: => Result): AuthenticatedActionBuilder[T] = notAuthenticatedAsync((req: Request[T]) => Future.successful(result))

  /**
    * Sets the roles that are allowed to access this endpoint.  When not invoked or invoked with an empty set all roles
    * are allowed.  Otherwise only users with one of the given roles will invoke the authenticated block, all others will
    * invoke the notAuthenticated block.
    *
    * @param roles the authenticated roles for this request
    * @return an action for only the given roles
    */
  def forRoles(roles: Set[String]) = copy(authorizedRoles = roles)

  override def parser: BodyParser[T] = bodyParser

  /**
    * Implementation of the action interface.
    * Verifies the provided access tokens.  If they are expired the refresh tokens are used.
    * If the refresh tokens can create a new access token and add it to the response's cookies.
    * @param request the incoming request
    * @return the response
    */
  override def apply(request: Request[T]): Future[Result] = {
    verifiedWithAccessToken(request)
      .orElse(verifiedWithRefreshToken(request))
      .getOrElse(notAuthenticated.apply(request))
  }

  def hasCorrectRole(account: Account): Boolean =
    if (authorizedRoles.isEmpty) true
    else account.getGroups().map(_.getName).exists(authorizedRoles)

  private def verifiedWithAccessToken(request: Request[T]): Option[Future[Result]] = {
    val accessToken: Option[Cookie] = request.cookies.get(AccessTokenName)

    accessToken.flatMap(a => verifyAccessToken(a.value))
        .filter(hasCorrectRole)
        .map(a => authenticated.apply(request, a))
  }

  private def verifiedWithRefreshToken(request: Request[T]) = {
    val refreshToken: Option[Cookie] = request.cookies.get(RefreshTokenName)

    refreshToken.flatMap(c => verifyRefreshToken(c.value))
        .filter(res => hasCorrectRole(res._2))
      .map(resultAccount => authenticated.apply(request, resultAccount._2)
        .map(_.withCookies(createCookiesFromResult(resultAccount._1):_*)))
  }
}