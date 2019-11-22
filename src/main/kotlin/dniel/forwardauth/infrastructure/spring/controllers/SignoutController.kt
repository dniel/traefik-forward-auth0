package dniel.forwardauth.infrastructure.spring.controllers

import dniel.forwardauth.application.CommandDispatcher
import dniel.forwardauth.application.SignoutHandler
import dniel.forwardauth.domain.shared.Application
import dniel.forwardauth.infrastructure.spring.exceptions.ApplicationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

/**
 * Sign out controller.
 */
@RestController
internal class SignoutController(val signoutHandler: SignoutHandler,
                                 val commandDispatcher: CommandDispatcher) : BaseController() {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    /**
     * Sign Out endpoint.
     *
     * @param headers
     * @param response
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/signout", method = [RequestMethod.GET])
    fun signout(@RequestHeader headers: MultiValueMap<String, String>,
                @RequestHeader("x-forwarded-host") forwardedHost: String,
                @CookieValue("ACCESS_TOKEN", required = false) accessToken: String,
                response: HttpServletResponse): ResponseEntity<Unit> {
        val command: SignoutHandler.SignoutCommand = SignoutHandler.SignoutCommand(forwardedHost, accessToken)
        val signoutEvent = commandDispatcher.dispatch(signoutHandler, command) as SignoutHandler.SignoutEvent

        return when (signoutEvent) {
            is SignoutHandler.SignoutEvent.SignoutDone -> {
                clearSessionCookies(signoutEvent.app, response)
                ResponseEntity.noContent().build()
            }
            is SignoutHandler.SignoutEvent.SignoutRedirect -> {
                clearSessionCookies(signoutEvent.app, response)
                ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).header("location", signoutEvent.redirectUrl).build()
            }
            is SignoutHandler.SignoutEvent.Error -> throw ApplicationException(signoutEvent.reason)
        }
    }

    /**
     * Remove session cookies from browser.
     */
    private fun clearSessionCookies(app: Application, response: HttpServletResponse) {
        LOGGER.debug("Clear session cookues, access token and id token.")
        clearCookie(response, "ACCESS_TOKEN", app.tokenCookieDomain)
        clearCookie(response, "JWT_TOKEN", app.tokenCookieDomain)
    }
}
