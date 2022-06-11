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

package dniel.forwardauth.infrastructure.micronaut.security

import dniel.forwardauth.application.CommandDispatcher
import dniel.forwardauth.application.commandhandlers.AuthenticateHandler
import dniel.forwardauth.domain.config.ForwardAuthSettings
import dniel.forwardauth.infrastructure.micronaut.exceptions.AuthenticationException
import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.token.jwt.validator.JwtTokenValidator
import io.micronaut.security.token.validator.TokenValidator
import jakarta.inject.Singleton
import mu.KotlinLogging
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux

private val logger = KotlinLogging.logger {}

/**
 * ## Validate token content.
 *
 * TODO
 * - should verify the content of the token.
 */
@Singleton
@Replaces(JwtTokenValidator::class)
class Auth0TokenValidator(val properties: ForwardAuthSettings,
                          val authenticateHandler: AuthenticateHandler,
                          val commandDispatcher: CommandDispatcher) : TokenValidator {

    /**
     * ## Dummy validate token.
     */
    override fun validateToken(token: String, request: HttpRequest<*>): Publisher<Authentication> {
        logger.info { "Validate token $token" }
        val accessToken = readCookie(request, "ACCESS_TOKEN")
        val idToken = readCookie(request, "ID_TOKEN")
        val host = request.headers["x-forwarded-host"]

        // authorize user and return the authenticated user object or anonymous user object
        return Flux.just(authorize(accessToken, idToken, host))
    }

    /**
     * Authorize user.
     * Set the current user in the SecurityContextHolder or
     * set Anonymous user if no valid user found.
     */
    fun authorize(accessToken: String?, idToken: String?, host: String): Auth0Authentication {
        // execute command and get result event.
        val command: AuthenticateHandler.AuthenticateCommand = AuthenticateHandler.AuthenticateCommand(accessToken, idToken, host)
        val event = commandDispatcher.dispatch(authenticateHandler, command) as AuthenticateHandler.AuthenticationEvent
        return when (event) {
            is AuthenticateHandler.AuthenticationEvent.Error -> {
                throw AuthenticationException(event)
            }
            is AuthenticateHandler.AuthenticationEvent.AuthenticatedUser -> {
                Auth0Authentication.fromUser(event.user)
            }
            is AuthenticateHandler.AuthenticationEvent.AnonymousUser -> {
                Auth0Authentication.fromUser(event.user)
            }
        }
    }

    fun readCookie(req: HttpRequest<*>, key: String): String? {
        return req.cookies[key]?.value
    }
}