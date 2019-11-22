package dniel.forwardauth.application

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.domain.authorize.service.Authenticator
import dniel.forwardauth.domain.events.Event
import dniel.forwardauth.domain.shared.Anonymous
import dniel.forwardauth.domain.shared.User
import dniel.forwardauth.domain.shared.service.VerifyTokenService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handle Sign in of user redirected from IDP service after giving Authorization to sign in.
 *
 */
@Component
class SigninHandler(val properties: AuthProperties,
                    val verifyTokenService: VerifyTokenService) : CommandHandler<SigninHandler.SigninCommand> {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    /**
     * This is the input parameter object for the handler to pass inn all
     * needed parameters to the handler.
     */
    data class SigninCommand(val something: String) : Command


    /**
     * This command can produce a set of events as response from the handle method.
     */
    sealed class SigninEvent(val user: User) : Event() {
        class Error(error: Authenticator.Error?) : SigninEvent(Anonymous) {
            val reason: String = error?.message ?: "Unknown error"
        }
    }

    /**
     * Main handle Sign in method.
     * This method will parse all input parameters from the command and validate a sign in
     * redirect response coming from Auth0 containing the result of the user authorization to
     * sign in or eventual errors if something happened when the user authorized sign in.
     * <p/>
     * @return an sign in event containing the result status of the sign in.
     */
    override fun handle(params: SigninHandler.SigninCommand): Event {
        TODO("implement command handler.")
    }

}