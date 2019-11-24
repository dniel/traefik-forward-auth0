package dniel.forwardauth.infrastructure.spring.controllers

import dniel.forwardauth.application.CommandDispatcher
import dniel.forwardauth.application.commandhandlers.UserinfoHandler
import dniel.forwardauth.domain.shared.Authenticated
import dniel.forwardauth.infrastructure.siren.Root
import dniel.forwardauth.infrastructure.siren.Siren.APPLICATION_SIREN_JSON
import dniel.forwardauth.infrastructure.spring.exceptions.ApplicationException
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
     * Sign Out endpoint.
     *
     * @param headers
     * @param response
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/userinfo", method = [RequestMethod.GET], produces = [APPLICATION_SIREN_JSON])
    fun signout(@RequestHeader headers: MultiValueMap<String, String>,
                @CookieValue("ACCESS_TOKEN", required = true) accessToken: String,
                authentication: Authentication): ResponseEntity<Root> {
        val authenticated = authentication.principal as Authenticated
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
