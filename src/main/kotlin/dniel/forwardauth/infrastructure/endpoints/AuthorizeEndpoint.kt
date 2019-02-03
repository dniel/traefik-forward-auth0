package dniel.forwardauth.infrastructure.endpoints

import com.auth0.jwt.interfaces.Claim
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
                  @HeaderParam("x-forwarded-host") forwardedHostHeader: String,
                  @HeaderParam("x-forwarded-proto") forwardedProtoHeader: String,
                  @HeaderParam("x-forwarded-uri") forwardedUriHeader: String,
                  @HeaderParam("x-forwarded-method") forwardedMethodHeader: String,
                  @HeaderParam("x-forward-auth-app") forwardAuthAppHeader: String?): Response {

        printHeaders(headers)
        return authenticateToken(accessTokenCookie, userinfoCookie, forwardAuthAppHeader, forwardedMethodHeader, forwardedHostHeader, forwardedProtoHeader, forwardedUriHeader)
    }

    private fun authenticateToken(accessTokenCookie: Cookie?, userinfoCookie: Cookie?, forwardAuthAppHeader: String?, forwardedMethodHeader: String, forwardedHostHeader: String, forwardedProtoHeader: String, forwardedUriHeader: String): Response {
        LOGGER.debug("AuthenticateToken: $forwardedProtoHeader://$forwardedHostHeader$forwardedUriHeader [accessToken=${accessTokenCookie != null}, jwt=${userinfoCookie != null}]");
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
        val verifyAccessToken = app.verifyAccessToken
        val idTokenClaims = app.idTokenClaims
        val accessTokenClaims = app.accessTokenClaims

        val originUrl = OriginUrl(forwardedProtoHeader, forwardedHostHeader, forwardedUriHeader)
        val nonce = nonceService.create()
        val state = State.create(originUrl, nonce)
        val authorizeUrl = AuthorizeUrl(AUTHORIZE_URL, audience, scopes.split(" ").toTypedArray(), clientId, redirectUrl, state)
        val nonceCookie = NewCookie("AUTH_NONCE", nonce.toString(), "/", tokenCookieDomain, null, -1, false);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("REDIRECT_URL = $redirectUrl")
            LOGGER.trace("AUDIENCE  = $audience")
            LOGGER.trace("ORIGIN_URL  = $originUrl")
            LOGGER.trace("AUTH_URL  = $authorizeUrl")
            LOGGER.trace("NONCE = $nonce")
            LOGGER.trace("STATE = $state")
            LOGGER.trace("SCOPES = $scopes")
            LOGGER.trace("CLIENT_ID = $clientId")
            LOGGER.trace("COOKIE DOMAIN = $tokenCookieDomain")
            LOGGER.trace("RESTRICTED_METHODS = ${restrictedMethods.joinToString()}")
            LOGGER.trace("VERIFY_ACCESS_TOKEN = $verifyAccessToken")
            LOGGER.trace("ID_TOKEN_CLAIMS = ${idTokenClaims.joinToString()}")
            LOGGER.trace("ACCESS_TOKEN_CLAIMS = ${accessTokenClaims.joinToString()}")
        }

        if (originUrl.startsWith(redirectUrl) || !restrictedMethods.contains(forwardedMethodHeader)) {
            LOGGER.info("AuthenticateToken NonRestrictedUrl, Access granted to $forwardedProtoHeader://$forwardedHostHeader$forwardedUriHeader")
            return Response.ok().build()
        }
        if (accessToken == null || userinfo == null) {
            LOGGER.info("AuthenticateToken MissingToken, Access denied to $forwardedProtoHeader://$forwardedHostHeader$forwardedUriHeader")
            return Response.temporaryRedirect(authorizeUrl.toURI()).cookie(nonceCookie).build()
        }

        // TODO clean up duplicated code below.
        val response = Response.ok().header("Authenticatation", "Bearer: ${accessToken}")
        if (app.verifyAccessToken == null || (app.verifyAccessToken != null && app.verifyAccessToken != false)) {
            val token = verifyTokenService.verify(accessToken, audience, DOMAIN)
            accessTokenClaims.forEach {
                val claimValue = getClaimValue(token.value.getClaim(it))
                if (claimValue != null) {
                    val headerName = "X-ForwardAuth-${it.toUpperCase()}"
                    if(LOGGER.isTraceEnabled()){
                        LOGGER.trace("AuthenticateToken AddClaimToHeader header=$headerName claim=$it value=$claimValue")
                    }
                    response.header(headerName, claimValue)
                }else{
                    LOGGER.warn("AuthenticateToken ClaimIgnored claim=$it")
                }
            }
        }
        try {
            val decodedUserToken = verifyTokenService.verify(userinfo, clientId, DOMAIN)
            idTokenClaims.forEach {
                val claimValue = getClaimValue(decodedUserToken.value.getClaim(it))
                if (claimValue != null) {
                    val headerName = "X-ForwardAuth-${it.toUpperCase()}"
                    if(LOGGER.isTraceEnabled()){
                        LOGGER.trace("AuthenticateToken AddClaimToHeader header=$headerName claim=$it value=$claimValue")
                    }

                    response.header(headerName, claimValue)
                }else{
                    LOGGER.warn("AuthenticateToken ClaimIgnored claim=$it")
                }
            }
            LOGGER.info("AuthenticateToken ValidToken, access granted to $forwardedProtoHeader://$forwardedHostHeader$forwardedUriHeader")
        } catch (e: Exception) {
            return Response.temporaryRedirect(authorizeUrl.toURI()).cookie(nonceCookie).build()
        }
        return response.build()
    }

    private fun getClaimValue(claim: Claim): String? {
        val claimValue = when {
            claim.asArray(String::class.java) != null -> {
                claim.asArray(String::class.java).joinToString()
            }
            claim.asBoolean() != null -> {
                claim.asBoolean().toString()
            }
            claim.asString() != null -> {
                claim.asString().toString()
            }
            claim.asLong() != null -> {
                claim.asLong().toString()
            }
            else -> null
        }
        return claimValue
    }

    private fun printHeaders(headers: HttpHeaders) {
        if (LOGGER.isTraceEnabled) {
            for (requestHeader in headers.requestHeaders) {
                LOGGER.trace("Header ${requestHeader.key} = ${requestHeader.value}")
            }
        }
    }

}