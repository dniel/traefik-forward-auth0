package dniel.forwardauth.application.commandhandlers

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.application.Command
import dniel.forwardauth.application.CommandHandler
import dniel.forwardauth.domain.events.Event
import dniel.forwardauth.domain.shared.Application
import dniel.forwardauth.infrastructure.auth0.Auth0Client
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handle Logout of user.
 *
 */
@Component
class LogoutHandler(val properties: AuthProperties,
                    val auth0Client: Auth0Client) : CommandHandler<LogoutHandler.LogoutCommand> {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    /**
     * This is the input parameter object for the handler to pass inn all
     * needed parameters to the handler.
     * @param accessToken is the user token to use for the signout request to the IDP
     * @param forwardedHost is the name of the host used to signout.
     */
    data class LogoutCommand(val forwardedHost: String,
                             val accessToken: String) : Command


    /**
     * This command can produce a set of events as response from the handle method.
     */
    sealed class LogoutEvent(val app: Application) : Event() {
        class LogoutComplete(app: Application) : LogoutEvent(app)
        class LogoutRedirect(val redirectUrl: String, app: Application) : LogoutEvent(app)
        class Error(val reason: String = "Unknown error", app: Application) : LogoutEvent(app)
    }

    /**
     * Main handle Sign out method.
     * <p/>
     * @return an sign out event containing the result status of the sign out.
     */
    override fun handle(params: LogoutCommand): Event {
        LOGGER.debug("Sign out from Auth0")
        val app = properties.findApplicationOrDefault(params.forwardedHost)
        try {
            val logout = auth0Client.logout(app.clientId, app.logoutUri)
            if (!logout.isNullOrEmpty()) {
                LOGGER.debug("Logout done, redirect to ${logout}")
                return LogoutEvent.LogoutRedirect(logout, app)
            } else {
                LOGGER.debug("Logout done.")
                return LogoutEvent.LogoutComplete(app)
            }
        } catch (e: Exception) {
            return LogoutEvent.Error(e.message!!, app)
        }
    }
}