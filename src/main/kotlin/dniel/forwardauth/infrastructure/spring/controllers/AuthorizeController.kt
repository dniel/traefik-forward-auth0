package dniel.forwardauth.infrastructure.spring.controllers

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.application.CommandDispatcher
import dniel.forwardauth.application.commandhandlers.AuthorizeHandler
import dniel.forwardauth.domain.shared.Authenticated
import dniel.forwardauth.domain.shared.User
import dniel.forwardauth.infrastructure.spring.exceptions.AuthorizationException
import dniel.forwardauth.infrastructure.spring.exceptions.PermissionDeniedException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.servlet.http.HttpServletResponse


/**
 * Authorize Endpoint.
 * This endpoint is used by ForwardAuth to authorize requests.
 */
@RestController
class AuthorizeController(val authorizeHandler: AuthorizeHandler, val commandDispatcher: CommandDispatcher, val authProperties: AuthProperties) : BaseController() {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    /**
     * Authorize Endpoint
     */
    @Operation(
            summary = "Authorize requests.",
            description = "This endpoint is called by Traefik to check if a request is authorized to access.",
            responses = arrayOf(
                    ApiResponse(
                            responseCode = "200",
                            description = "Access granted according to configuration in ForwardAuth and Auth0.",
                            content = arrayOf(Content())
                    ),
                    ApiResponse(
                            responseCode = "401",
                            description = "Access denied according to configuration in ForwardAuth and Auth0."
                    ),
                    ApiResponse(
                            responseCode = "307",
                            description = "Redirect for authentication with Auth0",
                            content = arrayOf(Content())
                    )
            )
    )
    @RequestMapping("/authorize", method = [RequestMethod.GET])
    fun authorize(@Parameter(hidden = true) @RequestHeader headers: MultiValueMap<String, String>,
                  @Parameter(description = "Requested content type", required = false, `in` = ParameterIn.HEADER) @RequestHeader("Accept", required = false) acceptContent: String?,
                  @Parameter(description = "Indicating ajax call", required = false, `in` = ParameterIn.HEADER) @RequestHeader("x-requested-with", required = false) requestedWithHeader: String?,
                  @Parameter(description = "Requested host", required = true, `in` = ParameterIn.HEADER) @RequestHeader("x-forwarded-host", required = true) forwardedHostHeader: String,
                  @Parameter(description = "Requested protocol", required = true, `in` = ParameterIn.HEADER) @RequestHeader("x-forwarded-proto", required = true) forwardedProtoHeader: String,
                  @Parameter(description = "Requested uri", required = true, `in` = ParameterIn.HEADER) @RequestHeader("x-forwarded-uri", required = true) forwardedUriHeader: String,
                  @Parameter(description = "Requested method", required = true, `in` = ParameterIn.HEADER) @RequestHeader("x-forwarded-method", required = true) forwardedMethodHeader: String,
                  response: HttpServletResponse): ResponseEntity<Unit> {
        printHeaders(headers)
        val user = SecurityContextHolder.getContext().authentication.principal as User
        return authenticateToken(acceptContent, requestedWithHeader, forwardedMethodHeader,
                forwardedHostHeader, forwardedProtoHeader, forwardedUriHeader, response, user)
    }


    /**
     * Authenticate
     *
     */
    private fun authenticateToken(acceptContent: String?, requestedWithHeader: String?, method: String, host: String, protocol: String,
                                  uri: String, response: HttpServletResponse, user: User): ResponseEntity<Unit> {
        val authorizeResult = handleCommand(acceptContent, requestedWithHeader, protocol, host, uri, method, user)
        return when (authorizeResult) {
            is AuthorizeHandler.AuthorizeEvent.AccessDenied -> throw PermissionDeniedException(authorizeResult)
            is AuthorizeHandler.AuthorizeEvent.Error -> throw AuthorizationException(authorizeResult)
            is AuthorizeHandler.AuthorizeEvent.NeedRedirect -> {
                redirect(authorizeResult, response)
            }
            is AuthorizeHandler.AuthorizeEvent.AccessGranted -> {
                accessGranted(authorizeResult)
            }
        }
    }

    /**
     * Execute AuthorizeCommand
     *
     */
    private fun handleCommand(acceptContent: String?, requestedWithHeader: String?,
                              protocol: String, host: String, uri: String, method: String, user: User): AuthorizeHandler.AuthorizeEvent {
        val isApi = (acceptsApiContent(acceptContent)) ||
                requestedWithHeader != null && requestedWithHeader == "XMLHttpRequest"

        val command: AuthorizeHandler.AuthorizeCommand = AuthorizeHandler.AuthorizeCommand(user, protocol, host, uri, method, isApi)
        return commandDispatcher.dispatch(authorizeHandler, command) as AuthorizeHandler.AuthorizeEvent
    }

    /**
     * Access Granted.
     * When user is Authenticated, add userinfo to headers to
     * forward userinfo from tokens to requested resource server.
     * When user is Anonymous just and access granted, just say 200 with no userinfo.
     */
    private fun accessGranted(authorizeResult: AuthorizeHandler.AuthorizeEvent.AccessGranted): ResponseEntity<Unit> {
        LOGGER.debug("Access Granted to ${authorizeResult.user}")
        val builder = ResponseEntity.noContent()
        when {
            authorizeResult.user is Authenticated -> {
                val accessToken = authorizeResult.user.accessToken.raw
                builder.header("Authorization", "Bearer ${accessToken}")
                authorizeResult.user.userinfo.forEach { k, v ->
                    val headerName = "x-forwardauth-${k.replace('_', '-')}".toLowerCase(Locale.ENGLISH)
                    LOGGER.trace("Add header ${headerName} with value ${v}")
                    builder.header(headerName, v)
                }
            }
        }
        return builder.build()
    }

    /**
     * Redirect to Authorize
     *
     */
    private fun redirect(authorizeResult: AuthorizeHandler.AuthorizeEvent.NeedRedirect, response: HttpServletResponse): ResponseEntity<Unit> {
        LOGGER.debug("Redirect to ${authorizeResult.authorizeUrl}")

        // add the nonce value to the request to be able to retrieve ut again on the singin endpoint.
        addCookie(response, "AUTH_NONCE", authorizeResult.nonce.value, authorizeResult.cookieDomain, authProperties.nonceMaxAge)
        return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).location(authorizeResult.authorizeUrl).build()
    }

    private fun acceptsApiContent(acceptContent: String?) =
            acceptContent != null &&
                    (acceptContent.contains("application/json") || acceptContent.contains("text/event-stream"))
}
