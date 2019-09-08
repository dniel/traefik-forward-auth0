package dniel.forwardauth.infrastructure.spring

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.domain.State
import dniel.forwardauth.domain.service.VerifyTokenService
import dniel.forwardauth.infrastructure.auth0.Auth0Client
import dniel.forwardauth.infrastructure.spring.exceptions.ApplicationErrorException
import dniel.forwardauth.infrastructure.spring.exceptions.PermissionDeniedException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import java.util.stream.Collectors
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

/**
 * Callback Endpoint for Auth0 signin to retrieve JWT token from code.
 * TODO rename to signin
 */
@RestController
class SigninController(val properties: AuthProperties, val auth0Client: Auth0Client, val verifyTokenService: VerifyTokenService) {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)
    private val DOMAIN = properties.domain

    /**
     * Sign In Callback Endpoint.
     * Use Code from signin query parameter to retrieve Token from Auth0 and decode and verify it.
     * http://auth.example.test/signin?error=unauthorized&error_description=Access%20denied.
     *
     * @param code
     * @param state
     * @param nonce
     * @param headers
     * @param response
     * @param error
     * @param errorDescription
     */
    @RequestMapping("/signin", method = [RequestMethod.GET])
    fun signin(@RequestHeader headers: MultiValueMap<String, String>,
               @RequestParam("code", required = false) code: String,
               @RequestParam("error", required = false) error: String,
               @RequestParam("error_description", required = false) errorDescription: String,
               @RequestParam("state", required = false) state: String,
               @RequestHeader("x-forwarded-host") forwardedHost: String,
               @CookieValue("AUTH_NONCE") nonce: String,
               response: HttpServletResponse): ResponseEntity<String> {
        printHeaders(headers)

        // if error parameter was received something is going on.
        if (error === "unauthorized") {
            LOGGER.info("Unauthorized error from Auth0 on sign in: ${errorDescription}")
            throw PermissionDeniedException(errorDescription)
        } else if (error.isNotEmpty()) {
            LOGGER.error("Signing received unknown error from Auth0 on sign in: ${errorDescription}")
            throw ApplicationErrorException(errorDescription)
        }

        LOGGER.debug("Sign in with code=$code")
        val decodedState = State.decode(state)
        val receivedNonce = decodedState.nonce.value
        if (receivedNonce != nonce) {
            LOGGER.error("SignInFailedNonce received=$receivedNonce sent=$nonce")
            throw ApplicationErrorException("Nonce cookie didnt match the nonce in state.")
        }

        val app = properties.findApplicationOrDefault(forwardedHost)
        val authorizationCodeExchangeResponse = auth0Client.authorizationCodeExchange(code, app.clientId, app.clientSecret, app.redirectUri)
        val accessToken = authorizationCodeExchangeResponse.get("access_token") as String
        val expiresIn = authorizationCodeExchangeResponse.get("expires_in") as Int
        val idToken = authorizationCodeExchangeResponse.get("id_token") as String

        val accessTokenCookie = Cookie("ACCESS_TOKEN", accessToken)
        val jwtCookie = Cookie("JWT_TOKEN", idToken)
        val clearNonceCookie = Cookie("AUTH_NONCE", "delete")

        accessTokenCookie.domain = app.tokenCookieDomain
        accessTokenCookie.maxAge = expiresIn
        accessTokenCookie.path = "/"
        accessTokenCookie.isHttpOnly = true
        jwtCookie.domain = app.tokenCookieDomain
        jwtCookie.maxAge = expiresIn
        jwtCookie.path = "/"
        jwtCookie.isHttpOnly = true
        clearNonceCookie.maxAge = 0
        clearNonceCookie.domain = app.tokenCookieDomain
        clearNonceCookie.path = "/"
        clearNonceCookie.isHttpOnly = true

        response.addCookie(accessTokenCookie)
        response.addCookie(jwtCookie)
        response.addCookie(clearNonceCookie)

        LOGGER.info("SignInSuccessful, redirect to requested originUrl=${decodedState.originUrl}")
        return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).location(decodedState.originUrl.uri()).build()
    }

    private fun printHeaders(headers: MultiValueMap<String, String>) {
        if (LOGGER.isTraceEnabled) {
            headers.forEach { (key, value) -> LOGGER.trace(String.format("Header '%s' = %s", key, value.stream().collect(Collectors.joining("|")))) }
        }
    }
}