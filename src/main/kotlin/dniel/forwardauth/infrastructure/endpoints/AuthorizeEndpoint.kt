package dniel.forwardauth.infrastructure.endpoints

import dniel.forwardauth.application.AuthorizeCommandHandler
import dniel.forwardauth.application.AuthorizeCommandHandler.AuthorizeCommand
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.ws.rs.*
import javax.ws.rs.core.*

@Path("authorize")
@Component
class AuthorizeEndpoint(val authorizeCommandHandler: AuthorizeCommandHandler) {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

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
                  @HeaderParam("x-forwarded-method") forwardedMethodHeader: String): Response {

        printHeaders(headers)
        return authenticateToken(accessTokenCookie, userinfoCookie, forwardedMethodHeader, forwardedHostHeader, forwardedProtoHeader, forwardedUriHeader)
    }

    private fun authenticateToken(accessTokenCookie: Cookie?, idTokenCookie: Cookie?, method: String, host: String, protocol: String, uri: String): Response {
        var accessToken = accessTokenCookie?.value
        val idToken = idTokenCookie?.value

        val command: AuthorizeCommand = AuthorizeCommand(accessToken, idToken, protocol, host, uri, method)
        val authorizeResult = authorizeCommandHandler.perform(command)

        authorizeResult.redirectUrl?.let {
            LOGGER.debug("Redirect to ${authorizeResult.redirectUrl}")
            val nonceCookie = NewCookie("AUTH_NONCE", authorizeResult.nonce.toString(), "/", authorizeResult.app.tokenCookieDomain, null, -1, false, true);
            return Response.temporaryRedirect(authorizeResult.redirectUrl).cookie(nonceCookie).build()
        }

        authorizeResult.accessToken?.let {
            val response = Response.ok()
            response.header("Authorization", "Bearer: ${authorizeResult.accessToken}")
            authorizeResult.userinfo.forEach { k, v ->
                val headerName = "X-AUTH-${k.toUpperCase()}"
                response.header(headerName, v)
                LOGGER.trace("Add header ${headerName} with value ${v}")
            }
            return response.build()
        }

        return Response.status(Response.Status.BAD_REQUEST).build()
    }


    private fun printHeaders(headers: HttpHeaders) {
        if (LOGGER.isTraceEnabled) {
            for (requestHeader in headers.requestHeaders) {
                LOGGER.trace("Header ${requestHeader.key} = ${requestHeader.value}")
            }
        }
    }

}