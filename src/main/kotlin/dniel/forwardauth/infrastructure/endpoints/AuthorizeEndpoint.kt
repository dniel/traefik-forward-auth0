package dniel.forwardauth.infrastructure.endpoints

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.domain.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.ws.rs.*
import javax.ws.rs.core.*

@Path("authorize")
@Component
class AuthorizeEndpoint(val properties: AuthProperties) {
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
                  @CookieParam("ACCESS_TOKEN") accessToken: Cookie?,
                  @CookieParam("JWT_TOKEN") jwt: Cookie?,
                  @HeaderParam("x-forwarded-host") forwardedHost: String,
                  @HeaderParam("x-forwarded-proto") forwardedProto: String,
                  @HeaderParam("x-forwarded-uri") forwardedUri: String): Response {
        LOGGER.debug("Authorize $forwardedProto://$forwardedHost$forwardedUri [accessToken=${accessToken != null}, jwt=${jwt != null}]");
        for (requestHeader in headers.requestHeaders) {
            LOGGER.debug("Header ${requestHeader.key} = ${requestHeader.value}")
        }

        val application = properties.findApplicationOrDefault(forwardedHost)
        val redirectUrl = application.redirectUri
        val audience = application.audience
        val scopes = application.scope
        val clientId = application.clientId
        val tokenCookieDomain = application.tokenCookieDomain

        val originUrl = OriginUrl(forwardedProto, forwardedHost, forwardedUri)
        val nonce = Nonce.create()
        val state = State.create(originUrl, nonce)
        val authorizeUrl = AuthorizeUrl(AUTHORIZE_URL, audience, scopes.split(" ").toTypedArray(), clientId, redirectUrl, state)
        val nonceCookie = NewCookie("AUTH_NONCE", nonce.toString(), "/", tokenCookieDomain, null, -1, false);

        LOGGER.debug("REDIRECT_URL = $redirectUrl")
        LOGGER.debug("AUDIENCE  = $audience")
        LOGGER.debug("ORIGIN_URL  = $originUrl")
        LOGGER.debug("NONCE = $nonce")
        LOGGER.debug("STATE = $state")
        LOGGER.debug("SCOPES = $scopes")
        LOGGER.debug("CLIENT_ID = $clientId")
        LOGGER.debug("COOKIE DOMAIN = $tokenCookieDomain")
        LOGGER.debug("JWT_TOKEN = $jwt")
        LOGGER.debug("ACCESS_TOKEN = $accessToken")

        if (originUrl.startsWith(redirectUrl)) {
            LOGGER.debug("Access granted to $forwardedProto://$forwardedHost$forwardedUri")
            return Response.ok().build()
        }
        if (accessToken == null) {
            LOGGER.debug("Access denied to $forwardedProto://$forwardedHost$forwardedUri")
            return Response.temporaryRedirect(authorizeUrl.toURI()).cookie(nonceCookie).build()
        }

        try {
            val decodedAccessToken = Token.verify(accessToken.value, audience, DOMAIN)
            val response = Response
                    .ok()
                    .header("X-Auth-User", decodedAccessToken.value.subject)
                    .header("X-Auth-Scope", "todo")

            if (jwt != null) {
                val decodedUserToken = Token.verify(jwt.value, audience, DOMAIN)
                response.header("X-Auth-Name", decodedUserToken.value.getClaim("name").asString())
                response.header("X-Auth-Nick", decodedUserToken.value.getClaim("nickname").asString())
                response.header("X-Auth-Email", decodedUserToken.value.getClaim("email").asString())
                response.header("X-Auth-Picture", decodedUserToken.value.getClaim("picture").asString())
            }
            LOGGER.info("Access granted to $forwardedProto://$forwardedHost$forwardedUri, user=${decodedAccessToken.value.subject}")
            return response.build()
        } catch (e: Exception) {
            return Response.temporaryRedirect(authorizeUrl.toURI()).cookie(nonceCookie).build()
        }
    }
}