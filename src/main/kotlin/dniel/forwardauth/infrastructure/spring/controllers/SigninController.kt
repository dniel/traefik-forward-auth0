package dniel.forwardauth.infrastructure.spring.controllers

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.domain.authorize.AuthorizeState
import dniel.forwardauth.infrastructure.auth0.Auth0Client
import dniel.forwardauth.infrastructure.spring.exceptions.ApplicationException
import dniel.forwardauth.infrastructure.spring.exceptions.Auth0Exception
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import java.util.stream.Collectors
import javax.servlet.http.HttpServletResponse

/**
 * Callback Endpoint for Auth0 signin to retrieve JWT token from code.
 */
@RestController
class SigninController(val properties: AuthProperties, val auth0Client: Auth0Client) : BaseController() {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

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
               @RequestParam("code", required = false) code: String?,
               @RequestParam("error", required = false) error: String?,
               @RequestParam("error_description", required = false) errorDescription: String?,
               @RequestParam("state", required = false) state: String?,
               @RequestHeader("x-forwarded-host") forwardedHost: String?,
               @CookieValue("AUTH_NONCE") nonce: String?,
               response: HttpServletResponse): ResponseEntity<String> {
        printHeaders(headers)

        // if error parameter was received something is going on.
        if (!error.isNullOrEmpty()) {
            LOGGER.error("Signing received unknown error from Auth0 on sign in: ${errorDescription}")
            throw Auth0Exception(error, errorDescription ?: "no error description.")
        }

        LOGGER.debug("Sign in with code=$code")
        if (!code.isNullOrBlank() && !state.isNullOrBlank()) {
            val decodedState = AuthorizeState.decode(state)
            val receivedNonce = decodedState.nonce.value
            if (receivedNonce != nonce) {
                LOGGER.error("SignInFailedNonce received=$receivedNonce sent=$nonce")
                throw ApplicationException("AuthorizeNonce cookie didnt match the nonce in authorizeState.")
            }

            val app = properties.findApplicationOrDefault(forwardedHost)
            val authorizationCodeExchangeResponse = auth0Client.authorizationCodeExchange(code, app.clientId, app.clientSecret, app.redirectUri)
            val accessToken = authorizationCodeExchangeResponse.get("access_token") as String
            val expiresIn = authorizationCodeExchangeResponse.get("expires_in") as Int
            val idToken = authorizationCodeExchangeResponse.get("id_token") as String

            addCookie(response, "ACCESS_TOKEN", accessToken, app.tokenCookieDomain, expiresIn)
            addCookie(response, "JWT_TOKEN", idToken, app.tokenCookieDomain, expiresIn)
            clearCookie(response, "AUTH_NONCE", app.tokenCookieDomain)

            LOGGER.info("SignInSuccessful, redirect to requested originUrl=${decodedState.originUrl}")
            return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).location(decodedState.originUrl.uri()).build()
        } else {
            LOGGER.error("Unknown request, login redirect request from Auth0 had no code, authorizeState or error message.")
            throw ApplicationException()
        }
    }

    private fun printHeaders(headers: MultiValueMap<String, String>) {
        if (LOGGER.isTraceEnabled) {
            headers.forEach { (key, value) -> LOGGER.trace(String.format("Header '%s' = %s", key, value.stream().collect(Collectors.joining("|")))) }
        }
    }
}