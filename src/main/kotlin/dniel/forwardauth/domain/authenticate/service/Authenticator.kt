/*
 * Copyright (c)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dniel.forwardauth.domain.authenticate.service

import dniel.forwardauth.domain.*
import org.slf4j.LoggerFactory

/**
 * Authenticator.
 *
 * @see AuthenticatorStateMachine for configuration of state machine.
 *
 */
class Authenticator private constructor(
    val accessToken: Token,
    val idToken: Token
) : AuthenticatorStateMachine.Delegate {

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
        log("onStartAuthentication")
        fsm.post(AuthenticatorStateMachine.Event.VALIDATE_TOKENS)
    }

    override fun onStartValidateTokens() {
        log("onStartValidateTokens")
        fsm.post(AuthenticatorStateMachine.Event.VALIDATE_ACCESS_TOKEN)
    }

    override fun onValidateAccessToken() {
        log("onValidateAccessToken")
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
        log("onValidateIdToken")
        when (idToken) {
            /**
             * Handling the case when ID Token is empty because
             * authenticating using client credentials where you
             * dont get an id token as response, only access token.
             */
            is EmptyToken -> {
                if (accessToken is JwtToken && accessToken.isClientCredentials()) {
                    fsm.post(AuthenticatorStateMachine.Event.EMPTY_ID_TOKEN)
                } else {
                    lastError = Error("An ID Token cant be empty unless using client credentials authentication flow.")
                    fsm.post(AuthenticatorStateMachine.Event.INVALID_ID_TOKEN)
                }
            }
            is JwtToken -> fsm.post(AuthenticatorStateMachine.Event.VALID_ID_TOKEN)
            is InvalidToken -> {
                lastError = Error(idToken.reason)
                fsm.post(AuthenticatorStateMachine.Event.INVALID_ID_TOKEN)
            }
            is OpaqueToken -> {
                lastError = Error("An ID Token can't be an opaque token.")
                fsm.post(AuthenticatorStateMachine.Event.INVALID_ID_TOKEN)
            }
        }
    }

    override fun onValidateSameSubs() {
        log("onValidateSameSubs")
        fun hasSameSubs(accessToken: Token, idToken: Token) =
            accessToken is JwtToken && idToken is JwtToken && idToken.subject() == accessToken.subject()

        // check if both tokens have the same subject
        if (hasSameSubs(accessToken, idToken)) {
            fsm.post(AuthenticatorStateMachine.Event.VALID_SUBS)
        } else {
            lastError = Error("Access Token and Id Token had different value in SUB-claim.")
            fsm.post(AuthenticatorStateMachine.Event.INVALID_SUBS)
        }
    }

    override fun onInvalidToken() {
        log("onInvalidToken")
        log(lastError!!.message)
    }

    override fun onError() {
        log("onError")
        log(lastError!!.message)
    }

    override fun onAuthenticated() {
        log("onAuthenticated")
    }

    override fun onAnonymous() {
        log("onAnonymous")
    }

    /*
     */
    fun authenticate(): AuthenticatorResult {
        return AuthenticatorResult(fsm.authenticate(), this.lastError)
    }

    fun log(message: String) {
        LOGGER.trace(message)
    }
}