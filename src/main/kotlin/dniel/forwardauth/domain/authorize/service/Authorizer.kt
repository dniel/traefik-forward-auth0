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

package dniel.forwardauth.domain.authorize.service

import dniel.forwardauth.domain.InvalidToken
import dniel.forwardauth.domain.JwtToken
import dniel.forwardauth.domain.OpaqueToken
import dniel.forwardauth.domain.Token
import dniel.forwardauth.domain.authorize.RequestedUrl
import dniel.forwardauth.domain.config.ApplicationSettings
import org.slf4j.LoggerFactory

/**
 * Authorizer.
 * This service is responsible for authorizing access for a requested url.
 * To handle all the logic involved to authorize the request a state machine is
 * created and all inputs from this class is used as context to find out if
 * the request can be authorized.
 *
 * @see AuthorizerStateMachine for configuration of state machine.
 *
 */
class Authorizer(
        private val accessToken: Token,
        private val app: ApplicationSettings,
        private val originUrl: RequestedUrl,
        override val isApi: Boolean
) : AuthorizerStateMachine.Delegate {

    private var fsm: AuthorizerStateMachine = AuthorizerStateMachine(this)

    companion object Factory {
        val LOGGER = LoggerFactory.getLogger(this::class.java)

        fun create(
                accessToken: Token,
                app: ApplicationSettings,
                originUrl: RequestedUrl,
                isApi: Boolean
        ): Authorizer = Authorizer(accessToken, app, originUrl, isApi)
    }

    /**
     * To return the resulting state from the State Machine, and also if an error has
     * occured, this result objects is returned from the authorize() method.
     */
    data class AuthorizerResult(val state: AuthorizerStateMachine.State, val error: Error?)

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

    override fun onStartAuthorizing() {
        log("onStartAuthorizing")
        log("Authorize request with $app")

        fsm.post(AuthorizerStateMachine.Event.VALIDATE_REQUESTED_URL)
    }

    override fun onValidateProtectedUrl() {
        log("onValidateProtectedUrl")
        fsm.post(AuthorizerStateMachine.Event.VALIDATE_WHITELISTED_URL)
    }

    override fun onValidateWhitelistedUrl() {
        log("onValidateWhitelistedUrl")

        fun isSigninUrl(originUrl: RequestedUrl, app: ApplicationSettings) =
                originUrl.startsWith(app.redirectUri)

        if (isSigninUrl(originUrl, app)) {
            fsm.post(AuthorizerStateMachine.Event.WHITELISTED_URL)
        } else {
            fsm.post(AuthorizerStateMachine.Event.RESTRICTED_URL)
        }
    }

    override fun onValidateRestrictedMethod() {
        log("onValidateRestrictedMethod")
        val method = originUrl.method
        fun isRestrictedMethod(app: ApplicationSettings, method: String) =
                app.restrictedMethods.any { t ->
                    log("Validate restricted method $t is $method")
                    t.equals(method, true) }

        when {
            isRestrictedMethod(app, method) -> fsm.post(AuthorizerStateMachine.Event.RESTRICTED_METHOD)
            else -> fsm.post(AuthorizerStateMachine.Event.UNRESTRICTED_METHOD)
        }
    }

    override fun onStartValidateTokens() {
        log("onStartValidateTokens")
        fsm.post(AuthorizerStateMachine.Event.VALIDATE_ACCESS_TOKEN)
    }

    override fun onValidateAccessToken() {
        log("onValidateAccessToken")
        when {
            accessToken is OpaqueToken -> {
                lastError = Error("Opaque Access Tokens is not supported.")
                fsm.post(AuthorizerStateMachine.Event.ERROR)
            }
            accessToken is JwtToken -> fsm.post(AuthorizerStateMachine.Event.VALID_ACCESS_TOKEN)
            accessToken is InvalidToken -> fsm.post(AuthorizerStateMachine.Event.INVALID_ACCESS_TOKEN)
        }
    }

    override fun onValidatePermissions() {
        log("onValidatePermissions")
        val jwtAccessToken = accessToken as JwtToken
        when {
            app.requiredPermissions.isNotEmpty() && !jwtAccessToken.hasPermissionClaim() -> {
                LOGGER.warn("# Missing permissions claim in access token. In Auth0, Add Permissions in the Access Token.")
                LOGGER.warn("# If this setting is enabled, the Permissions claim will be added to the access token.")
                lastError = Error("Missing permissions claim in access token. In Auth0, Add Permissions in the Access Token.")
                fsm.post(AuthorizerStateMachine.Event.INVALID_PERMISSIONS)
            }
            jwtAccessToken.hasPermission(app.requiredPermissions) -> fsm.post(AuthorizerStateMachine.Event.VALID_PERMISSIONS)
            else -> {
                val missingPermissions = jwtAccessToken.missingPermissions(app.requiredPermissions)
                lastError = Error("Missing permissions '${missingPermissions.joinToString()}'")
                fsm.post(AuthorizerStateMachine.Event.INVALID_PERMISSIONS)
            }
        }
    }

    override fun onNeedRedirect() {
        log("onNeedRedirect")
    }

    override fun onInvalidToken() {
        log("onInvalidToken")
    }

    override fun onError() {
        log("onError")
        log(lastError!!.message)
    }

    override fun onAccessGranted() {
        log("onAccessGranted")
    }

    override fun onAccessDenied() {
        log("onAccessDenied")
    }

    fun authorize(): AuthorizerResult {
        return AuthorizerResult(fsm.authorize(), this.lastError)
    }

    fun log(message: String) {
        LOGGER.trace(message)
    }
}