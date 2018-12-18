package dniel.forwardauth.infrastructure.endpoints

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.domain.State
import dniel.forwardauth.domain.service.VerifyTokenService
import dniel.forwardauth.infrastructure.auth0.Auth0Service
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.ws.rs.*
import javax.ws.rs.core.*


/**
 * Callback Endpoint for Auth0 signin to retrieve JWT token from code.
 * TODO rename to signin
 */
@Path("signin")
@Component
class SigninEndpoint(val properties: AuthProperties, val auth0Client: Auth0Service, val verifyToken: VerifyTokenService) {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)
    val DOMAIN = properties.domain

    /**
     * Callback Endpoint
     * Use Code from signin query parameter to retrieve Token from Auth0 and decode and verify it.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun signin(@Context headers: HttpHeaders,
               @QueryParam("code") code: String,
               @QueryParam("state") state: String,
               @HeaderParam("x-forwarded-host") forwardedHost: String,
               @CookieParam("AUTH_NONCE") nonceCookie: Cookie): Response {
        LOGGER.info("Signin with code=$code")
        for (requestHeader in headers.requestHeaders) {
            LOGGER.info("Header ${requestHeader.key} = ${requestHeader.value}")
        }

        val app = properties.findApplicationOrDefault(forwardedHost)
        val audience = app.audience
        val tokenCookieDomain = app.tokenCookieDomain

        val decodedState = State.decode(state)
        if (decodedState.nonce.value != nonceCookie.value) {
            LOGGER.error("Failed nonce check")
        }

        val response = auth0Client.authorizationCodeExchange(code, app.clientId, app.clientSecret, app.redirectUri)
        val access_token = response.get("access_token") as String
        val id_token = response.get("id_token") as String

        val decodedAccessToken = verifyToken.verify(access_token, audience, DOMAIN)
        val accessTokenCookie = NewCookie("ACCESS_TOKEN", access_token, "/", tokenCookieDomain, null, -1, false)
        val expiresAt = NewCookie("EXPIRES_AT", "" + decodedAccessToken.value.expiresAt.time, "/", tokenCookieDomain, null, -1, false)
        val jwtCookie = NewCookie("JWT_TOKEN", id_token, "/", tokenCookieDomain, null, -1, false)
        val nonceCookie = NewCookie("AUTH_NONCE", "deleted", "/", tokenCookieDomain, null, 0, false)

        LOGGER.info("Redirect to originUrl originUrl=${decodedState.originUrl}")
        return Response
                .temporaryRedirect(decodedState.originUrl.uri())
                .cookie(jwtCookie)
                .cookie(expiresAt)
                .cookie(accessTokenCookie)
                .cookie(nonceCookie)
                .build()
    }
}