package dniel.forwardauth.application

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.domain.events.Event
import dniel.forwardauth.domain.shared.Application
import dniel.forwardauth.infrastructure.auth0.Auth0Client
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handle Sign out of user.
 *
 */
@Component
class SignoutHandler(val properties: AuthProperties,
                     val auth0Client: Auth0Client) : CommandHandler<SignoutHandler.SignoutCommand> {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    /**
     * This is the input parameter object for the handler to pass inn all
     * needed parameters to the handler.
     * @param accessToken is the user token to use for the signout request to the IDP
     * @param forwardedHost is the name of the host used to signout.
     */
    data class SignoutCommand(val forwardedHost: String,
                              val accessToken: String) : Command


    /**
     * This command can produce a set of events as response from the handle method.
     */
    sealed class SignoutEvent(val app: Application) : Event() {
        class SignoutDone(app: Application) : SignoutEvent(app)
        class SignoutRedirect(val redirectUrl: String, app: Application) : SignoutEvent(app)
        class Error(val reason: String = "Unknown error", app: Application) : SignoutEvent(app)
    }

    /**
     * Main handle Sign out method.
     * <p/>
     * @return an sign out event containing the result status of the sign out.
     */
    override fun handle(params: SignoutHandler.SignoutCommand): Event {
        LOGGER.debug("Sign out from Auth0")
        val app = properties.findApplicationOrDefault(params.forwardedHost)
        try {
            val signout = auth0Client.signout(app.clientId, app.returnTo)
            if (!signout.isNullOrEmpty()) {
                LOGGER.debug("Signout done, redirect to ${signout}")
                return SignoutEvent.SignoutRedirect(signout, app)
            } else {
                LOGGER.debug("Signout done.")
                return SignoutEvent.SignoutDone(app)
            }
        } catch (e: Exception) {
            return SignoutEvent.Error(e.message!!, app)
        }
    }
}