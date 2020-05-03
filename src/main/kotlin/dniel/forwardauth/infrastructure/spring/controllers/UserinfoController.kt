package dniel.forwardauth.infrastructure.spring.controllers

import dniel.forwardauth.application.CommandDispatcher
import dniel.forwardauth.application.commandhandlers.UserinfoHandler
import dniel.forwardauth.domain.shared.Authenticated
import dniel.forwardauth.domain.shared.User
import dniel.forwardauth.infrastructure.siren.Root
import dniel.forwardauth.infrastructure.siren.Siren.APPLICATION_SIREN_JSON
import dniel.forwardauth.infrastructure.spring.exceptions.ApplicationException
import io.swagger.v3.oas.annotations.ExternalDocumentation
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*

@RestController
internal class UserinfoController(val userinfoHandler: UserinfoHandler,
                                  val commandDispatcher: CommandDispatcher) {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    /**
     * Userinfo endpoint.
     *
     * @param headers
     * @param response
     */
    @Operation(
            tags = arrayOf("userinfo"),
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
                                            mediaType = APPLICATION_SIREN_JSON))
                    ),
                    ApiResponse(
                            responseCode = "401",
                            description = "If no authenticated user.",
                            content = arrayOf(Content())
                    )
            ))
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/userinfo", method = [RequestMethod.GET], produces = [APPLICATION_SIREN_JSON])
    fun userinfo(@Parameter(description = "Access token for current user in Cookie", required = false, `in` = ParameterIn.COOKIE) @CookieValue("ACCESS_TOKEN", required = false) accessTokenCookie: String?,
                 @Parameter(description = "Access token for current user in Header", required = false, `in` = ParameterIn.HEADER) @RequestHeader("Authorization", required = false) accessTokenHeader: String?,
                 @Parameter(hidden = true) authentication: Authentication): ResponseEntity<Root> {
        val authenticated = authentication.principal as User
        val command: UserinfoHandler.UserinfoCommand = UserinfoHandler.UserinfoCommand(authenticated)
        val userinfoEvent = commandDispatcher.dispatch(userinfoHandler, command) as UserinfoHandler.UserinfoEvent

        return when (userinfoEvent) {
            is UserinfoHandler.UserinfoEvent.Userinfo -> {
                val root = Root.newBuilder()
                        .title("Userinfo for ${authentication.name}")
                        .properties(userinfoEvent.properties)
                        .clazz("userinfo")
                        .build()
                ResponseEntity.ok(root)
            }
            is UserinfoHandler.UserinfoEvent.Error -> throw ApplicationException(userinfoEvent.reason)
        }
    }
}
