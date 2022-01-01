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
import dniel.forwardauth.domain.events.Event
import dniel.forwardauth.infrastructure.auth0.Auth0Client
import dniel.forwardauth.domain.config.ApplicationSettings
import dniel.forwardauth.infrastructure.micronaut.config.ForwardAuthSettings
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

/**
 * Handle Sign out of user.
 *
 */
@Singleton
class SignoutHandler(
        val properties: ForwardAuthSettings,
        val auth0Client: Auth0Client
) : CommandHandler<SignoutHandler.SignoutCommand> {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    /**
     * This is the input parameter object for the handler to pass inn all
     * needed parameters to the handler.
     * @param accessToken is the user token to use for the signout request to the IDP
     * @param forwardedHost is the name of the host used to signout.
     */
    data class SignoutCommand(
        val forwardedHost: String,
        val accessToken: String
    ) : Command

    /**
     * This command can produce a set of events as response from the handle method.
     */
    sealed class SignoutEvent(val app: ApplicationSettings) : Event() {
        class SignoutComplete(app: ApplicationSettings) : SignoutEvent(app)
        class SignoutRedirect(val redirectUrl: String, app: ApplicationSettings) : SignoutEvent(app)
        class Error(val reason: String = "Unknown error", app: ApplicationSettings) : SignoutEvent(app)
    }

    /**
     * Main handle Sign out method.
     * <p/>
     * @return an sign out event containing the result status of the sign out.
     */
    override fun handle(params: SignoutCommand): Event {
        LOGGER.debug("Sign out from Auth0")
        val app = properties.findApplicationOrDefault(params.forwardedHost)
        try {
            val signout = auth0Client.signout(app.clientId, app.returnTo)
            if (!signout.isNullOrEmpty()) {
                LOGGER.debug("Signout done, redirect to $signout")
                return SignoutEvent.SignoutRedirect(signout, app)
            } else {
                LOGGER.debug("Signout done.")
                return SignoutEvent.SignoutComplete(app)
            }
        } catch (e: Exception) {
            return SignoutEvent.Error(e.message!!, app)
        }
    }
}