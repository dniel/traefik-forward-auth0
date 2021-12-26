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

package dniel.forwardauth.application.commandhandlers

import com.auth0.jwt.interfaces.Claim
import dniel.forwardauth.application.Command
import dniel.forwardauth.application.CommandHandler
import dniel.forwardauth.domain.*
import dniel.forwardauth.domain.authenticate.service.Authenticator
import dniel.forwardauth.domain.authenticate.service.AuthenticatorStateMachine
import dniel.forwardauth.domain.events.Event
import dniel.forwardauth.domain.shared.*
import dniel.forwardauth.domain.shared.service.VerifyTokenService
import dniel.forwardauth.infrastructure.micronaut.config.Application
import dniel.forwardauth.infrastructure.micronaut.config.ApplicationConfig
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

/**
 * Handle Authentication.
 * This class handles authentication of a user and will produce a result of either an authenticated user
 * or an anonymous user. The logic for how to perform an authentication is separated out to the
 * state machine in its own service to be maintainable.
 *
 * @see dniel.forwardauth.domain.authorize.service.AuthenticatorStateMachine
 * @see dniel.forwardauth.domain.authorize.service.Authenticator
 *
 */
@Singleton
class AuthenticateHandler(
    val properties: ApplicationConfig,
    val verifyTokenService: VerifyTokenService
) : CommandHandler<AuthenticateHandler.AuthenticateCommand> {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    /**
     * This is the input parameter object for the handler to pass inn all
     * needed parameters to the handler.
     */
    data class AuthenticateCommand(
        val accessToken: String?,
        val idToken: String?,
        val host: String
    ) : Command

    /**
     * This command can produce a set of events as response from the handle method.
     */
    sealed class AuthenticationEvent(val user: User) : Event() {
        class AuthenticatedUser(user: User) : AuthenticationEvent(user)
        class AnonymousUser : AuthenticationEvent(Anonymous)
        class Error(error: Authenticator.Error?) : AuthenticationEvent(Anonymous) {
            val reason: String = error?.message ?: "Unknown error"
        }
    }

    /**
     * Main handle Authentication method.
     * This method will parse all input parameters from the command, create a new
     * instance of the Authenticator service to perform the authentication logic.
     * <p/>
     * The result will be either an Authenticated user or an Anonymous user.
     */
    override fun handle(params: AuthenticateCommand): Event {
        val app = properties.findApplicationOrDefault(params.host)
        val accessToken = verifyTokenService.verify(params.accessToken, app.audience)
        val idToken = verifyTokenService.verify(params.idToken, app.clientId)

        val authenticator = Authenticator.create(accessToken, idToken)
        val (state, error) = authenticator.authenticate()
        LOGGER.debug("State: $state, Error: $error")

        return when (state) {
            AuthenticatorStateMachine.State.ANONYMOUS -> AuthenticationEvent.AnonymousUser()
            AuthenticatorStateMachine.State.AUTHENTICATED -> {
                val userinfoFromToken = getUserinfoFromToken(app, idToken, accessToken)
                val user = Authenticated(accessToken as JwtToken, idToken, userinfoFromToken)
                AuthenticationEvent.AuthenticatedUser(user)
            }
            else -> AuthenticationEvent.Error(error)
        }
    }

    /**
     * Get selected userinfo from token claims.
     */
    private fun getUserinfoFromToken(app: Application, idToken: Token, accessToken: Token): Map<String, String> {
        app.claims.forEach { s -> LOGGER.trace("Should add Claim from token: $s") }
        val userinfo = mutableMapOf<String, String>()
        // use sub claim from access token.
        userinfo["sub"] = (accessToken as JwtToken).subject()

        // add rest of claims from id token.
        if (idToken is JwtToken) {
            userinfo.putAll(
                idToken.value.claims
                    .onEach { entry: Map.Entry<String, Claim> -> LOGGER.trace("Token Claim ${entry.key}=${getClaimValue(entry.value)}") }
                    .filterKeys { app.claims.contains(it) }
                    .onEach { entry: Map.Entry<String, Claim> -> LOGGER.trace("Filtered claim ${entry.key}=${getClaimValue(entry.value)}") }
                    .mapValues { getClaimValue(it.value) }
                    .filterValues { it != null } as Map<String, String>
            )
        }
        return userinfo
    }

    private fun getClaimValue(claim: Claim): String? {
        return when {
            claim.asArray(String::class.java) != null -> claim.asArray(String::class.java).joinToString()
            claim.asBoolean() != null -> claim.asBoolean().toString()
            claim.asString() != null -> claim.asString().toString()
            claim.asLong() != null -> claim.asLong().toString()
            else -> null
        }
    }
}