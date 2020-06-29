package dniel.forwardauth.infrastructure.spring.controllers

import dniel.forwardauth.application.CommandDispatcher
import dniel.forwardauth.application.commandhandlers.LoginHandler
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
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

/**
 * Login controller.
 */
@RestController
internal class LoginController(val loginHandler: LoginHandler,
                               val commandDispatcher: CommandDispatcher) : BaseController() {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    /**
     * Login endpoint.
     * Redirect the user to the authorize endpoint to login.
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
    @RequestMapping("/login", method = [RequestMethod.GET])
    fun login(@RequestHeader headers: MultiValueMap<String, String>,
                @RequestHeader("x-forwarded-host") forwardedHost: String,
                response: HttpServletResponse): ResponseEntity<Unit> {
        val command: LoginHandler.LoginCommand = LoginHandler.LoginCommand(forwardedHost)
        val loginEvent = commandDispatcher.dispatch(loginHandler, command) as LoginHandler.LoginEvent

        return when (loginEvent) {
            is LoginHandler.LoginEvent.LoginRedirect -> {
                // add the nonce value to the request to be able to retrieve ut again on the singin endpoint.
                addCookie(response, "AUTH_NONCE", loginEvent.nonce.value, loginEvent.tokenCookieDomain, loginEvent.maxNonceAge)
                return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).location(loginEvent.redirectUrl).build()
            }
            is LoginHandler.LoginEvent.Error -> throw ApplicationException(loginEvent.reason)
        }
    }
}
