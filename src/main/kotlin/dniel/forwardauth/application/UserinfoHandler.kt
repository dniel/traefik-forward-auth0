package dniel.forwardauth.application

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.domain.authorize.service.Authenticator
import dniel.forwardauth.domain.events.Event
import dniel.forwardauth.domain.shared.Anonymous
import dniel.forwardauth.domain.shared.User
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handle request for userinfo for current user.
 *
 */
@Component
class UserinfoHandler(val properties: AuthProperties) : CommandHandler<UserinfoHandler.UserinfoCommand> {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    /**
     * This is the input parameter object for the handler to pass inn all
     * needed parameters to the handler.
     */
    data class UserinfoCommand(val something: String) : Command


    /**
     * This command can produce a set of events as response from the handle method.
     */
    sealed class UserinfoEvent(val user: User) : Event() {
        class Error(error: Authenticator.Error?) : UserinfoEvent(Anonymous) {
            val reason: String = error?.message ?: "Unknown error"
        }
    }

    /**
     * Main handle Userinfo method.
     * <p/>
     * @return an userinfo event containing the result status of the userinfo.
     */
    override fun handle(params: UserinfoHandler.UserinfoCommand): Event {
        TODO("implement command handler.")
    }

}