package dniel.forwardauth.application

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.domain.authorize.AuthorizeNonce
import dniel.forwardauth.domain.authorize.AuthorizeState
import dniel.forwardauth.domain.authorize.AuthorizeUrl
import dniel.forwardauth.domain.authorize.RequestedUrl
import dniel.forwardauth.domain.authorize.service.Authorizer
import dniel.forwardauth.domain.authorize.service.AuthorizerStateMachine
import dniel.forwardauth.domain.events.Event
import dniel.forwardauth.domain.shared.Anonymous
import dniel.forwardauth.domain.shared.Application
import dniel.forwardauth.domain.shared.Authenticated
import dniel.forwardauth.domain.shared.User
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URI


/**
 * Handle Authorization.
 * This command handler will do all the checking if a user has access or not to a url.
 * As a result of all evaluations in the authorization logic the result will be a set
 * of AuthEvents that will be returned as the result from the handle method.
 * <p/>
 * The handle-method will take all the input and verify according to a set of rules
 * if the user has access the requested url.
 *
 * <p/>
 * Ideas to error handling
 * http://www.douevencode.com/articles/2018-09/kotlin-error-handling/
 * https://medium.com/@spaghetticode/finite-authorizeState-machines-in-kotlin-part-1-57e68d54d93b
 * https://www.codeproject.com/Articles/509234/The-State-Design-Pattern-vs-State-Machine
 * https://github.com/stateless4j
 */
@Component
class AuthorizeHandler(val properties: AuthProperties) : CommandHandler<AuthorizeHandler.AuthorizeCommand> {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    /**
     * This is the input parameter object for the handler to pass inn all
     * needed parameters to the handler.
     */
    data class AuthorizeCommand(
            val user: User,
            val protocol: String,
            val host: String,
            val uri: String,
            val method: String,
            val isApi: Boolean
    ) : Command


    /**
     * This command can produce a set of events as response from the handle method.
     */
    sealed class AuthorizeEvent(val user: User, val application: Application) : Event() {
        class NeedRedirect(application: Application, val authorizeUrl: URI, val nonce: AuthorizeNonce, val cookieDomain: String) : AuthorizeEvent(Anonymous, application)
        class AccessGranted(user: Authenticated, application: Application) : AuthorizeEvent(user, application)
        class AccessDenied(user: User, application: Application, error: Authorizer.Error?) : AuthorizeEvent(user, application) {
            val reason: String = error?.message ?: "Unknown error"
        }

        class Error(user: User, application: Application, error: Authorizer.Error?) : AuthorizeEvent(user, application) {
            val reason: String = error?.message ?: "Unknown error"
        }
    }

    /**
     * Main Handle Command method.
     */
    override fun handle(params: AuthorizeCommand): AuthorizeEvent {
        val authUrl = properties.authorizeUrl
        val app = properties.findApplicationOrDefault(params.host)
        val nonce = AuthorizeNonce.generate()
        val originUrl = RequestedUrl(params.protocol, params.host, params.uri, params.method)
        val state = AuthorizeState.create(originUrl, nonce)
        val authorizeUrl = AuthorizeUrl(authUrl, app, state)
        val cookieDomain = app.tokenCookieDomain
        val isApi = params.isApi

        val user = params.user
        val accessToken = user.accessToken
        val idToken = user.idToken

        val authorizer = Authorizer.create(accessToken, idToken, app, originUrl, isApi)
        val (authorizerState, authorizerError) = authorizer.authorize()

        LOGGER.debug("State: ${authorizerState}")
        LOGGER.debug("Error: ${authorizerError}")

        return when (authorizerState) {
            AuthorizerStateMachine.State.NEED_REDIRECT -> AuthorizeEvent.NeedRedirect(app, authorizeUrl.toURI(), nonce, cookieDomain)
            AuthorizerStateMachine.State.ACCESS_DENIED -> AuthorizeEvent.AccessDenied(user, app, authorizerError)
            AuthorizerStateMachine.State.ACCESS_GRANTED -> AuthorizeEvent.AccessGranted(user as Authenticated, app)
            else -> AuthorizeEvent.Error(user, app, authorizerError)
        }
    }
}