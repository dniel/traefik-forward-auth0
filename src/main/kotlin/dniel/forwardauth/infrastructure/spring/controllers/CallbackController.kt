package dniel.forwardauth.infrastructure.spring.controllers

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.application.CommandDispatcher
import dniel.forwardauth.application.commandhandlers.CallbackHandler
import dniel.forwardauth.infrastructure.spring.exceptions.ApplicationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

/**
 * Callback Endpoint for Auth0 signin to retrieve JWT token from code.
 */
@RestController
class CallbackController(val properties: AuthProperties,
                         val callbackHandler: CallbackHandler,
                         val commandDispatcher: CommandDispatcher) : BaseController() {
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
        val command: CallbackHandler.SigninCommand = CallbackHandler.SigninCommand(forwardedHost, code, error, errorDescription, state, nonce)
        val signinEvent = commandDispatcher.dispatch(callbackHandler, command) as CallbackHandler.SigninEvent

        return when (signinEvent) {
            is CallbackHandler.SigninEvent.SigninComplete -> {
                addCookie(response, "ACCESS_TOKEN", signinEvent.accessToken, signinEvent.app.tokenCookieDomain, signinEvent.expiresIn)
                addCookie(response, "JWT_TOKEN", signinEvent.idToken, signinEvent.app.tokenCookieDomain, signinEvent.expiresIn)
                clearCookie(response, "AUTH_NONCE", signinEvent.app.tokenCookieDomain)
                LOGGER.info("SignInSuccessful, redirect to '${signinEvent.redirectTo}'")
                ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).location(signinEvent.redirectTo).build()
            }
            is CallbackHandler.SigninEvent.Error -> throw ApplicationException(signinEvent.reason)
        }
    }
}