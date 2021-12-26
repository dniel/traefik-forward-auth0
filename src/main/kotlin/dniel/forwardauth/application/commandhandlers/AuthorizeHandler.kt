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

import dniel.forwardauth.application.Command
import dniel.forwardauth.application.CommandHandler
import dniel.forwardauth.domain.Anonymous
import dniel.forwardauth.domain.User
import dniel.forwardauth.domain.authorize.AuthorizeNonce
import dniel.forwardauth.domain.authorize.AuthorizeState
import dniel.forwardauth.domain.authorize.AuthorizeUrl
import dniel.forwardauth.domain.authorize.RequestedUrl
import dniel.forwardauth.domain.authorize.service.Authorizer
import dniel.forwardauth.domain.authorize.service.AuthorizerStateMachine
import dniel.forwardauth.domain.events.Event
import dniel.forwardauth.infrastructure.micronaut.config.Application
import dniel.forwardauth.infrastructure.micronaut.config.ApplicationConfig
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.net.URI

/**
 * Handle Authorization.
 * This command handler will do all the checking if a user has access or not to a url.
 * As a result of all evaluations in the authorization logic the result will be a set
 * of AuthEvents that will be returned as the result from the handle method.
 * <p/>
 * The authorization logic operates and make decissions on the content of the
 * Access Token claims to evalurate with the state machine if current user has
 * access to the url requested.
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
@Singleton
class AuthorizeHandler(val properties: ApplicationConfig) : CommandHandler<AuthorizeHandler.AuthorizeCommand> {

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
        class NeedRedirect(
            application: Application,
            val authorizeUrl: URI,
            val nonce: AuthorizeNonce,
            val cookieDomain: String
        ) : AuthorizeEvent(Anonymous, application)
        class AccessGranted(user: User, application: Application) : AuthorizeEvent(user, application)
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

        val authorizer = Authorizer.create(accessToken, app, originUrl, isApi)
        val (authorizerState, authorizerError) = authorizer.authorize()
        LOGGER.debug("State: $authorizerState, Error: $authorizerError")

        return when (authorizerState) {
            AuthorizerStateMachine.State.NEED_REDIRECT -> AuthorizeEvent.NeedRedirect(app, authorizeUrl.toURI(), nonce, cookieDomain)
            AuthorizerStateMachine.State.ACCESS_DENIED -> AuthorizeEvent.AccessDenied(user, app, authorizerError)
            AuthorizerStateMachine.State.ACCESS_GRANTED -> AuthorizeEvent.AccessGranted(user, app)
            else -> AuthorizeEvent.Error(user, app, authorizerError)
        }
    }
}