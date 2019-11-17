package dniel.forwardauth.application

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.domain.authorize.service.Authenticator
import dniel.forwardauth.domain.events.Event
import dniel.forwardauth.domain.shared.Anonymous
import dniel.forwardauth.domain.shared.User
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handle Sign out of user.
 *
 */
@Component
class SignoutHandler(val properties: AuthProperties) : CommandHandler<SignoutHandler.SignoutCommand> {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    /**
     * This is the input parameter object for the handler to pass inn all
     * needed parameters to the handler.
     */
    data class SignoutCommand(val something: String) : Command


    /**
     * This command can produce a set of events as response from the handle method.
     */
    sealed class SignoutEvent(val user: User) : Event() {
        class Error(error: Authenticator.Error?) : SignoutEvent(Anonymous) {
            val reason: String = error?.message ?: "Unknown error"
        }
    }

    /**
     * Main handle Sign out method.
     * <p/>
     * @return an sign out event containing the result status of the sign out.
     */
    override fun handle(params: SignoutHandler.SignoutCommand): Event {
        TODO("implement command handler.")
    }

}