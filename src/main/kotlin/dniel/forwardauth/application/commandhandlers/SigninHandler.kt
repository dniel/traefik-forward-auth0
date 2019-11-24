package dniel.forwardauth.application.commandhandlers

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.application.Command
import dniel.forwardauth.application.CommandHandler
import dniel.forwardauth.domain.authorize.AuthorizeState
import dniel.forwardauth.domain.events.Event
import dniel.forwardauth.domain.shared.Application
import dniel.forwardauth.infrastructure.auth0.Auth0Client
import dniel.forwardauth.infrastructure.spring.exceptions.ApplicationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URI

/**
 * Handle Sign in of user redirected from IDP service after giving Authorization to sign in.
 *
 */
@Component
class SigninHandler(val properties: AuthProperties,
                    val auth0Client: Auth0Client) : CommandHandler<SigninHandler.SigninCommand> {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    /**
     * This is the input parameter object for the handler to pass inn all
     * needed parameters to the handler.
     */
    data class SigninCommand(val forwardedHost: String?,
                             val code: String?,
                             val error: String?,
                             val errorDescription: String?,
                             val state: String?,
                             val nonce: String?) : Command


    /**
     * This command can produce a set of events as response from the handle method.
     */
    sealed class SigninEvent() : Event() {
        class SigninComplete(val accessToken: String,
                             val idToken: String,
                             val expiresIn: Int,
                             val redirectTo: URI,
                             val app: Application) : SigninEvent()

        class Error(val reason: String, val description: String) : SigninEvent()
    }

    /**
     * Main handle Sign in method.
     * This method will parse all input parameters from the command and validate a sign in
     * redirect response coming from Auth0 containing the result of the user authorization to
     * sign in or eventual errors if something happened when the user authorized sign in.
     * <p/>
     * @return an sign in event containing the result status of the sign in.
     */
    override fun handle(params: SigninCommand): Event {
        val app = properties.findApplicationOrDefault(params.forwardedHost)
        // if error parameter was received something is going on.
        if (!params.error.isNullOrEmpty()) {
            LOGGER.error("Signing received unknown error from Auth0 on sign in: ${params.errorDescription}")
            return SigninEvent.Error(params.error, params.errorDescription ?: "no error description.")
        }

        LOGGER.debug("Sign in with code=${params.code}")
        if (!params.code.isNullOrBlank() && !params.state.isNullOrBlank()) {
            val decodedState = AuthorizeState.decode(params.state)
            val receivedNonce = decodedState.nonce.value
            if (receivedNonce != params.nonce) {
                LOGGER.error("SignInFailedNonce received=$receivedNonce sent=${params.nonce}")
                throw ApplicationException("AuthorizeNonce cookie didnt match the nonce in authorizeState.")
            }

            val authorizationCodeExchangeResponse = auth0Client.authorizationCodeExchange(params.code, app.clientId, app.clientSecret, app.redirectUri)
            val accessToken = authorizationCodeExchangeResponse.get("access_token") as String
            val expiresIn = authorizationCodeExchangeResponse.get("expires_in") as Int
            val idToken = authorizationCodeExchangeResponse.get("id_token") as String
            return SigninEvent.SigninComplete(accessToken, idToken, expiresIn, decodedState.originUrl.uri(), app)
        } else {
            return SigninEvent.Error("Unknown request", "login redirect request from Auth0 had no code, authorizeState or error message.")
        }
    }

}