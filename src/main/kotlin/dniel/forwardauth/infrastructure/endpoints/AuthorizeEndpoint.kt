package dniel.forwardauth.infrastructure.endpoints

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.domain.AuthorizeUrl
import dniel.forwardauth.domain.OriginUrl
import dniel.forwardauth.domain.State
import dniel.forwardauth.domain.service.NonceService
import dniel.forwardauth.domain.service.VerifyTokenService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.ws.rs.*
import javax.ws.rs.core.*

@Path("authorize")
@Component
class AuthorizeEndpoint(val properties: AuthProperties,
                        val verifyTokenService: VerifyTokenService,
                        val nonceService: NonceService
) {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    val AUTHORIZE_URL = properties.authorizeUrl
    val DOMAIN = properties.domain

    /**
     * Authorize Endpoint.
     * This endpoint is used by traefik forward properties to authorize requests.
     * It will return 200 for requests that has a valid JWT_TOKEN and will
     * redirect other to authenticate at Auth0.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun authorize(@Context headers: HttpHeaders,
                  @CookieParam("ACCESS_TOKEN") accessTokenCookie: Cookie?,
                  @CookieParam("JWT_TOKEN") userinfoCookie: Cookie?,
                  @HeaderParam("x-client-id") clientIdHeader: String?,
                  @HeaderParam("x-client-secret") clientSecretHeader: String?,
                  @HeaderParam("x-audience") audienceHeader: String?,
                  @HeaderParam("x-forwarded-host") forwardedHostHeader: String,
                  @HeaderParam("x-forwarded-proto") forwardedProtoHeader: String,
                  @HeaderParam("x-forwarded-uri") forwardedUriHeader: String,
                  @HeaderParam("x-forward-auth-app") forwardAuthAppHeader: String?): Response {

        if (LOGGER.isDebugEnabled) {
            headers.requestHeaders.forEach { requestHeader -> LOGGER.debug("Header ${requestHeader.key} = ${requestHeader.value}") }
        }

        if (clientIdHeader != null && clientSecretHeader != null && audienceHeader != null) {
            return authenticateClientCredentials(clientIdHeader, clientSecretHeader, audienceHeader, forwardedProtoHeader, forwardedHostHeader, forwardedUriHeader)
        } else {
            return authenticateAccessToken(accessTokenCookie, userinfoCookie, forwardAuthAppHeader, forwardedHostHeader, forwardedProtoHeader, forwardedUriHeader)
        }
    }

    private fun authenticateAccessToken(accessTokenCookie: Cookie?, userinfoCookie: Cookie?, forwardAuthAppHeader: String?, forwardedHostHeader: String, forwardedProtoHeader: String, forwardedUriHeader: String): Response {
        LOGGER.debug("Authorize Access Token: $forwardedProtoHeader://$forwardedHostHeader$forwardedUriHeader [accessToken=${accessTokenCookie != null}, jwt=${userinfoCookie != null}]");
        var accessToken = accessTokenCookie?.value
        val userinfo = userinfoCookie?.value

        LOGGER.debug("USER_TOKEN = $userinfo")
        LOGGER.debug("ACCESS_TOKEN = $accessToken")

        val app = properties.findApplicationOrDefault(forwardedHostHeader)
        val redirectUrl = app.redirectUri
        val audience = app.audience
        val scopes = app.scope
        val clientId = app.clientId
        val tokenCookieDomain = app.tokenCookieDomain
        val restrictedMethods = app.restrictedMethods

        val originUrl = OriginUrl(forwardedProtoHeader, forwardedHostHeader, forwardedUriHeader)
        val nonce = nonceService.create()
        val state = State.create(originUrl, nonce)
        val authorizeUrl = AuthorizeUrl(AUTHORIZE_URL, audience, scopes.split(" ").toTypedArray(), clientId, redirectUrl, state)
        val nonceCookie = NewCookie("AUTH_NONCE", nonce.toString(), "/", tokenCookieDomain, null, -1, false);

        LOGGER.debug("REDIRECT_URL = $redirectUrl")
        LOGGER.debug("AUDIENCE  = $audience")
        LOGGER.debug("ORIGIN_URL  = $originUrl")
        LOGGER.debug("AUTH_URL  = $authorizeUrl")
        LOGGER.debug("NONCE = $nonce")
        LOGGER.debug("STATE = $state")
        LOGGER.debug("SCOPES = $scopes")
        LOGGER.debug("CLIENT_ID = $clientId")
        LOGGER.debug("COOKIE DOMAIN = $tokenCookieDomain")
        LOGGER.debug("RESTRICTED_METHODS = $restrictedMethods")

        if (originUrl.startsWith(redirectUrl)) {
            LOGGER.debug("Access granted to $forwardedProtoHeader://$forwardedHostHeader$forwardedUriHeader")
            return Response.ok().build()
        }
        if (accessToken == null) {
            LOGGER.debug("Access denied to $forwardedProtoHeader://$forwardedHostHeader$forwardedUriHeader")
            return Response.temporaryRedirect(authorizeUrl.toURI()).cookie(nonceCookie).build()
        }

        try {
            val decodedAccessToken = verifyTokenService.verify(accessToken, audience, DOMAIN)
            val response = Response.ok().header("Authenticatation", "Bearer: ${accessToken}")

            if (userinfo != null) {
                val decodedUserToken = verifyTokenService.verify(userinfo, clientId, DOMAIN)
                response.header("X-Auth-Name", decodedUserToken.value.getClaim("name").asString())
                        .header("X-Auth-User", decodedUserToken.value.subject)
                        .header("X-Auth-Nick", decodedUserToken.value.getClaim("nickname").asString())
                        .header("X-Auth-Email", decodedUserToken.value.getClaim("email").asString())
                        .header("X-Auth-Picture", decodedUserToken.value.getClaim("picture").asString())
            }
            LOGGER.info("Authorized Access Token, access granted to $forwardedProtoHeader://$forwardedHostHeader$forwardedUriHeader, user=${decodedAccessToken.value.subject}")
            return response.build()
        } catch (e: Exception) {
            return Response.temporaryRedirect(authorizeUrl.toURI()).cookie(nonceCookie).build()
        }
    }

    private fun authenticateClientCredentials(clientIdHeader: String, clientSecretHeader: String, audienceHeader: String, forwardedProtoHeader: String, forwardedHostHeader: String, forwardedUriHeader: String): Response {
        LOGGER.debug("Authorized Client Credentials: $forwardedProtoHeader://$forwardedHostHeader$forwardedUriHeader [clientId=${clientIdHeader}]")
        TODO("add verification that the client_id and client_secret that are trying to access the specific frontend in traefik actually has permission to access it.")
    }
}