package authentication

import authentication.AuthenticationGlobal._
import com.stormpath.sdk.account.Account
import com.stormpath.sdk.oauth.{Authenticators, OAuthBearerRequestAuthenticationResult, OAuthGrantRequestAuthenticationResult, OAuthRequests}
import io.jsonwebtoken.JwtException
import play.api.mvc.Cookie

/**
  * Created by Val Smith on 11/17/16.
  */
object StormpathAuthenticationService {

  def verifyUserPassword(user: String, password: String): Option[(OAuthGrantRequestAuthenticationResult, Account)] = {
    val authRequest = OAuthRequests.OAUTH_PASSWORD_GRANT_REQUEST
      .builder()
      .setLogin(user)
      .setPassword(password)
      .build()

    val result = Authenticators.OAUTH_PASSWORD_GRANT_REQUEST_AUTHENTICATOR
      .forApplication(application)
      .authenticate(authRequest)

    verifyAccessToken(result.getAccessTokenString).map(a => (result, a))
  }

  def createCookiesFromResult(result: OAuthGrantRequestAuthenticationResult): Seq[Cookie] = {
    Seq(
      Cookie(AccessTokenName, result.getAccessTokenString, Some(result.getExpiresIn.toInt), secure = false),
      Cookie(RefreshTokenName, result.getRefreshTokenString, Some(result.getExpiresIn.toInt), secure = false)
    )
  }

  def verifyAccessToken(accessToken: String): Option[Account] = {
    try {
      val result: OAuthBearerRequestAuthenticationResult = authenticateAccessToken(accessToken)
      Some(result.getAccount)
    } catch {
      case e: JwtException => None
    }
  }

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
