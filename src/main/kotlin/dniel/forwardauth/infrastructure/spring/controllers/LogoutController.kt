package dniel.forwardauth.infrastructure.spring.controllers

import dniel.forwardauth.application.CommandDispatcher
import dniel.forwardauth.application.commandhandlers.LogoutHandler
import dniel.forwardauth.domain.shared.Application
import dniel.forwardauth.infrastructure.siren.Siren
import dniel.forwardauth.infrastructure.spring.exceptions.ApplicationException
import io.swagger.v3.oas.annotations.ExternalDocumentation
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

/**
 * Logout controller.
 */
@RestController
internal class LogoutController(val logoutHandler: LogoutHandler,
                                val commandDispatcher: CommandDispatcher) : BaseController() {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    /**
     * Sign Out endpoint.
     *
     * @param headers
     * @param response
     */
    @Operation(
            tags = arrayOf("login"),
            summary = "Get userinfo",
            description = "Get userinfo of authenticated user.",
            responses = arrayOf(
                    ApiResponse(
                            responseCode = "200",
                            description = "Userinfo about the currently authenticated user.",
                            content = arrayOf(
                                    Content(
                                            schema = Schema(
                                                    externalDocs = ExternalDocumentation(
                                                            description = "Link to Siren Hypermedia specification",
                                                            url = "https://raw.githubusercontent.com/kevinswiber/siren/master/siren.schema.json")),
                                            mediaType = Siren.APPLICATION_SIREN_JSON))
                    ),
                    ApiResponse(
                            responseCode = "401",
                            description = "If no authenticated user.",
                            content = arrayOf(Content())
                    )
            ))
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/logout", method = [RequestMethod.GET])
    fun logout(@RequestHeader headers: MultiValueMap<String, String>,
               @RequestHeader("x-forwarded-host") forwardedHost: String,
               @CookieValue("ACCESS_TOKEN", required = false) accessToken: String,
               response: HttpServletResponse): ResponseEntity<Unit> {
        val command: LogoutHandler.LogoutCommand = LogoutHandler.LogoutCommand(forwardedHost, accessToken)
        val logoutEvent = commandDispatcher.dispatch(logoutHandler, command) as LogoutHandler.LogoutEvent

        return when (logoutEvent) {
            is LogoutHandler.LogoutEvent.LogoutComplete -> {
                clearSessionCookies(logoutEvent.app, response)
                ResponseEntity.noContent().build()
            }
            is LogoutHandler.LogoutEvent.LogoutRedirect -> {
                clearSessionCookies(logoutEvent.app, response)
                ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).header("location", logoutEvent.redirectUrl).build()
            }
            is LogoutHandler.LogoutEvent.Error -> throw ApplicationException(logoutEvent.reason)
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
