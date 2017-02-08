package authentication

import authentication.AuthenticationGlobal._
import com.stormpath.sdk.account.Account
import com.stormpath.sdk.oauth.{Authenticators, OAuthBearerRequestAuthenticationResult, OAuthGrantRequestAuthenticationResult, OAuthRequests}
import com.stormpath.sdk.resource.ResourceException
import io.jsonwebtoken.JwtException
import play.api.mvc.Cookie

/**
  * Created by Val Smith on 11/17/16.
  * This class provides utility methods for interacting with the Stormpath java API.
  */
object StormpathAuthenticationService {

  /**
    * Attempts a login with a plain string user and password.
    *
    * @param user Username
    * @param password Password
    * @return Result and account when authentication successful, None when fails
    */
  def verifyUserPassword(user: String, password: String): Option[(OAuthGrantRequestAuthenticationResult, Account)] = {
    val authRequest = OAuthRequests.OAUTH_PASSWORD_GRANT_REQUEST
      .builder()
      .setLogin(user)
      .setPassword(password)
      .build()

    try {
      val result = Authenticators.OAUTH_PASSWORD_GRANT_REQUEST_AUTHENTICATOR
        .forApplication(application)
        .authenticate(authRequest)
      verifyAccessToken(result.getAccessTokenString).map(a => (result, a))
    } catch {
      case e: ResourceException => None
    }
  }

  /**
    * Creates secure access cookies from a successful authentication
    *
    * @param result Successful authentication result
    * @return the cookies
    */
  def createCookiesFromResult(result: OAuthGrantRequestAuthenticationResult): Seq[Cookie] = {
    Seq(
      Cookie(AccessTokenName, result.getAccessTokenString, Some(result.getExpiresIn.toInt), secure = true),
      Cookie(RefreshTokenName, result.getRefreshTokenString, Some(result.getExpiresIn.toInt), secure = true)
    )
  }

  /**
    * Verifies a current access token and retrieves the Account information.  Returns None when the token is invalid.
    *
    * @param accessToken the token to verify
    * @return The verified account or None
    */
  def verifyAccessToken(accessToken: String): Option[Account] = {
    try {
      val result: OAuthBearerRequestAuthenticationResult = authenticateAccessToken(accessToken)
      Some(result.getAccount)
    } catch {
      case e: JwtException => None
    }
  }

  /**
    * Uses a refresh token to generate new credentials.  The credentials can be used to create new cookies.
    *
    * @param refreshToken the refresh token
    * @return The successful authentication result and account or None
    */
  def verifyRefreshToken(refreshToken: String): Option[(OAuthGrantRequestAuthenticationResult, Account)] = {
    try {
      val request = OAuthRequests.OAUTH_REFRESH_TOKEN_REQUEST
        .builder().setRefreshToken(refreshToken).build()
      val refreshResult = Authenticators.OAUTH_REFRESH_TOKEN_REQUEST_AUTHENTICATOR
        .forApplication(application).authenticate(request)
      Some((refreshResult, authenticateAccessToken(refreshResult.getAccessToken.getJwt).getAccount))
    } catch {
      case e: JwtException => None
    }
  }

  private def authenticateAccessToken(accessToken: String): OAuthBearerRequestAuthenticationResult = {
    val token = OAuthRequests.OAUTH_BEARER_REQUEST
      .builder()
      .setJwt(accessToken)
      .build()
    Authenticators.OAUTH_BEARER_REQUEST_AUTHENTICATOR
      .forApplication(application)
      .authenticate(token)
  }
}
