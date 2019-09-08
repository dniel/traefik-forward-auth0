package dniel.forwardauth.infrastructure.spring

import dniel.forwardauth.application.AuthorizeHandler
import dniel.forwardauth.infrastructure.spring.exceptions.PermissionDeniedException
import dniel.forwardauth.infrastructure.spring.exceptions.ApplicationErrorException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import java.util.stream.Collectors
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse


@RestController
class AuthorizeController(val authorizeHandler: AuthorizeHandler) {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    /**
     * Authorize Endpoint.
     * This endpoint is used by traefik forward properties to authorize requests.
     * It will return 200 for requests that has a valid JWT_TOKEN and will
     * redirect other to authenticate at Auth0.
     */
    @RequestMapping("/authorize", method = [RequestMethod.GET])
    fun authorize(@RequestHeader headers: MultiValueMap<String, String>,
                  @CookieValue("ACCESS_TOKEN", required = false) accessTokenCookie: String?,
                  @CookieValue("JWT_TOKEN", required = false) userinfoCookie: String?,
                  @RequestHeader("x-forwarded-host") forwardedHostHeader: String,
                  @RequestHeader("x-forwarded-proto") forwardedProtoHeader: String,
                  @RequestHeader("x-forwarded-uri") forwardedUriHeader: String,
                  @RequestHeader("x-forwarded-method") forwardedMethodHeader: String,
                  response: HttpServletResponse): ResponseEntity<Unit> {

        printHeaders(headers)
        return authenticateToken(accessTokenCookie, userinfoCookie, forwardedMethodHeader, forwardedHostHeader, forwardedProtoHeader, forwardedUriHeader, response)
    }

    private fun authenticateToken(accessToken: String?, idToken: String?, method: String, host: String, protocol: String, uri: String, response: HttpServletResponse): ResponseEntity<Unit> {
        val command: AuthorizeHandler.AuthorizeCommand = AuthorizeHandler.AuthorizeCommand(accessToken, idToken, protocol, host, uri, method)
        val authorizeResult = authorizeHandler.handle(command)
        authorizeResult.forEachIndexed { index, authEvent -> LOGGER.trace("AuthEvent #${index}: ${authEvent}") }

        // 1. check special sign in case
        // always let the sigin request through.
        if (authorizeResult.find {
                    it is AuthorizeHandler.AuthEvent.ValidSignInEvent
                } != null) {
            LOGGER.debug("Let the sign in request through.")
            return ResponseEntity.noContent().build()
        }

        // 2. check authentication is needed.
        val redirectEvent = authorizeResult.find {
            it is AuthorizeHandler.AuthEvent.NeedRedirectEvent
        } as AuthorizeHandler.AuthEvent.NeedRedirectEvent?
        if (redirectEvent != null) {
            // add the nonce value to the request to be able to retrieve ut again on the singin endpoint.
            val nonceCookie = Cookie("AUTH_NONCE", redirectEvent.nonce.value)
            nonceCookie.domain = redirectEvent.cookieDomain
            nonceCookie.maxAge = 60
            nonceCookie.isHttpOnly = true
            nonceCookie.path = "/"
            response.addCookie(nonceCookie)
            return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).location(redirectEvent.authorizeUrl).build()
        }

        // 3. check authorization.
        // check if we got a permission denied event in result.
        if (authorizeResult.find {
                    it is AuthorizeHandler.AuthEvent.PermissionDeniedEvent
                } != null) {
            throw PermissionDeniedException()
        }

        // if we managed to get all here through all three cases above, the user has access.
        val validIdTokenEvent = authorizeResult.find {
            it is AuthorizeHandler.AuthEvent.ValidIdTokenEvent
        } as AuthorizeHandler.AuthEvent.ValidIdTokenEvent?
        val validAccessTokenEvent = authorizeResult.find {
            it is AuthorizeHandler.AuthEvent.ValidAccessTokenEvent
        } as AuthorizeHandler.AuthEvent.ValidAccessTokenEvent?

        // it should really not be possible to end up here after all validation above.
        if (validIdTokenEvent == null || validAccessTokenEvent == null) {
            throw ApplicationErrorException()
        } else {
            val builder = ResponseEntity.noContent()

            // add the authorization bearer header with token so that
            // the backend api receives it and knows that the user has been authenticated.
            builder.header("Authorization", "Bearer ${accessToken}")
            validIdTokenEvent.userinfo.forEach { k, v ->
                val headerName = "X-Forwardauth-${k.capitalize()}"
                LOGGER.trace("Add header ${headerName} with value ${v}")
                builder.header(headerName, v)
            }

            return builder.build()
        }
    }


    private fun printHeaders(headers: MultiValueMap<String, String>) {
        if (LOGGER.isTraceEnabled) {
            headers.forEach { (key, value) -> LOGGER.trace(String.format("Header '%s' = %s", key, value.stream().collect(Collectors.joining("|")))) }
        }
    }
}