package dniel.forwardauth.domain.authorize.service

import dniel.forwardauth.domain.shared.InvalidToken
import dniel.forwardauth.domain.shared.JwtToken
import dniel.forwardauth.domain.shared.OpaqueToken
import dniel.forwardauth.domain.shared.Token
import org.slf4j.LoggerFactory

/**
 * Authenticator.
 *
 * @see AuthenticatorStateMachine for configuration of state machine.
 *
 */
class Authenticator private constructor(val accessToken: Token,
                                        val idToken: Token) : AuthenticatorStateMachine.Delegate {

    companion object Factory {
        val LOGGER = LoggerFactory.getLogger(this::class.java)

        fun create(accessToken: Token, idToken: Token):
                Authenticator = Authenticator(accessToken, idToken)
    }

    private var fsm: AuthenticatorStateMachine

    init {
        fsm = AuthenticatorStateMachine(this)
    }

    /**
     * To return the resulting state from the State Machine, and also if an error has
     * occured, this result objects is returned from the authorize() method.
     */
    data class AuthenticatorResult(val state: AuthenticatorStateMachine.State, val error: Error?)

    /**
     * Error object with error message, this is used to store last error that happened in state machine.
     */
    data class Error(val message: String)

    /**
     * When on of the event callbacks has failed, it signals
     * and error by setting the lastError variable.
     */
    private var lastError: Error? = null
    override val hasError: Boolean
        get() = lastError != null


    override fun onStartAuthentication() {
        trace("onStartAuthentication")
        fsm.post(AuthenticatorStateMachine.Event.VALIDATE_TOKENS)
    }

    override fun onStartValidateTokens() {
        trace("onStartValidateTokens")
        fsm.post(AuthenticatorStateMachine.Event.VALIDATE_ACCESS_TOKEN)
    }

    override fun onValidateAccessToken() {
        trace("onValidateAccessToken")
        when {
            accessToken is OpaqueToken -> {
                lastError = Error("Opaque Access Tokens is not supported.")
                fsm.post(AuthenticatorStateMachine.Event.ERROR)
            }
            accessToken is JwtToken -> fsm.post(AuthenticatorStateMachine.Event.VALID_ACCESS_TOKEN)
            accessToken is InvalidToken -> {
                lastError = Error(accessToken.reason)
                fsm.post(AuthenticatorStateMachine.Event.INVALID_ACCESS_TOKEN)
            }
        }
    }

    override fun onValidateIdToken() {
        trace("onValidateIdToken")
        when {
            idToken is JwtToken -> fsm.post(AuthenticatorStateMachine.Event.VALID_ID_TOKEN)
            idToken is InvalidToken -> {
                lastError = Error(idToken.reason)
                fsm.post(AuthenticatorStateMachine.Event.INVALID_ID_TOKEN)
            }
        }
    }

    override fun onValidateSameSubs() {
        trace("onValidateSameSubs")
        fun hasSameSubs(accessToken: Token, idToken: Token) =
                accessToken is JwtToken && idToken is JwtToken && idToken.subject()  == accessToken.subject()

        // check if both tokens have the same subject
        if (hasSameSubs(accessToken, idToken)) {
            fsm.post(AuthenticatorStateMachine.Event.VALID_SUBS)
        } else {
            lastError = Error("Access Token and Id Token had different value in SUB-claim.")
            fsm.post(AuthenticatorStateMachine.Event.INVALID_SUBS)
        }
    }

    override fun onInvalidToken() {
        trace("onInvalidToken")
        trace(lastError!!.message)
    }

    override fun onError() {
        trace("onError")
        trace(lastError!!.message)
    }

    override fun onAuthenticated() {
        trace("onAuthenticated")
    }

    override fun onAnonymous() {
        trace("onAnonymous")
    }


    /*
     */
    fun authenticate(): AuthenticatorResult {
        return AuthenticatorResult(fsm.authenticate(), this.lastError)
    }

    fun trace(message: String) {
        LOGGER.trace(message)
    }

}
