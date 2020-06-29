package dniel.forwardauth.application.commandhandlers

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.application.Command
import dniel.forwardauth.application.CommandHandler
import dniel.forwardauth.domain.authorize.AuthorizeNonce
import dniel.forwardauth.domain.authorize.AuthorizeState
import dniel.forwardauth.domain.authorize.AuthorizeUrl
import dniel.forwardauth.domain.authorize.RequestedUrl
import dniel.forwardauth.domain.events.Event
import dniel.forwardauth.domain.shared.Application
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URI

/**
 * Handle Logout of user.
 *
 */
@Component
class LoginHandler(private val properties: AuthProperties) : CommandHandler<LoginHandler.LoginCommand> {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    /**
     * This is the input parameter object for the handler to pass inn all
     * needed parameters to the handler.
     * @param forwardedHost is the name of the application used to signout.
     */
    data class LoginCommand(val forwardedHost: String) : Command


    /**
     * This command can produce a set of events as response from the handle method.
     */
    sealed class LoginEvent(val app: Application) : Event() {
        class LoginRedirect(val redirectUrl: URI,
                            val nonce: AuthorizeNonce,
                            val tokenCookieDomain: String,
                            val maxNonceAge: Int,
                            app: Application) : LoginEvent(app)

        class Error(val reason: String = "Unknown error", app: Application) : LoginEvent(app)
    }

    /**
     * Main handle Sign out method.
     * <p/>
     * @return an sign out event containing the result status of the sign out.
     */
    override fun handle(params: LoginCommand): Event {
        LOGGER.debug("Login with Auth0")
        val app = properties.findApplicationOrDefault(params.forwardedHost)

        // just abort if no login url is set, nowhere to redirect user after login.
        if(app.loginUri.isNullOrBlank()){
            return LoginEvent.Error("Missing login url in configuration.", app)
        }

        val authUrl = properties.authorizeUrl
        val nonce = AuthorizeNonce.generate()
        val loginUrl = URI.create(app.loginUri)
        val originUrl = RequestedUrl(loginUrl.scheme, loginUrl.host, loginUrl.path, "GET")
        val state = AuthorizeState.create(originUrl, nonce)
        val authorizeUrl = AuthorizeUrl(authUrl, app, state)
        val tokenCookieDomain = app.tokenCookieDomain
        val maxNonceAge = properties.nonceMaxAge

        try {
            return LoginEvent.LoginRedirect(authorizeUrl.toURI(), nonce, tokenCookieDomain, maxNonceAge, app)
        } catch (e: Exception) {
            return LoginEvent.Error(e.message!!, app)
        }
    }
}