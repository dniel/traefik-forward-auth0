package dniel.forwardauth.application.commandhandlers

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.application.Command
import dniel.forwardauth.application.CommandHandler
import dniel.forwardauth.domain.events.Event
import dniel.forwardauth.domain.shared.Authenticated
import dniel.forwardauth.domain.shared.User
import dniel.forwardauth.infrastructure.auth0.Auth0Client
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handle request for userinfo for current user.
 *
 */
@Component
class UserinfoHandler(val properties: AuthProperties,
                      val auth0Client: Auth0Client) : CommandHandler<UserinfoHandler.UserinfoCommand> {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    /**
     * This is the input parameter object for the handler to pass inn all
     * needed parameters to the handler.
     */
    data class UserinfoCommand(val user: User) : Command


    /**
     * This command can produce a set of events as response from the handle method.
     */
    sealed class UserinfoEvent(val user: User) : Event() {
        class Userinfo(val properties: Map<String, Any>, user: User) : UserinfoEvent(user)
        class Error(error: String, user: User) : UserinfoEvent(user) {
            val reason: String = error
        }
    }

    /**
     * Main handle Userinfo method.
     * <p/>
     * @return an userinfo event containing the result status of the userinfo.
     */
    override fun handle(params: UserinfoCommand): Event {
        return when (params.user) {
            is Authenticated -> {
                try {
                    LOGGER.debug("Get userinfo for user ${params.user.sub}")
                    val userinfo = auth0Client.userinfo(params.user.accessToken.raw)
                    UserinfoEvent.Userinfo(userinfo, params.user)
                } catch (e: Exception) {
                    UserinfoEvent.Error(e.message!!, params.user)
                }
            }
            else -> UserinfoEvent.Error("Unable to retrieve userinfo.", params.user)
        }
    }
}