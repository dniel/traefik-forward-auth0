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

package dniel.forwardauth.infrastructure.micronaut.controllers

import dniel.forwardauth.application.CommandDispatcher
import dniel.forwardauth.application.commandhandlers.SigninHandler
import dniel.forwardauth.infrastructure.micronaut.config.AuthProperties
import dniel.forwardauth.infrastructure.spring.exceptions.ApplicationException
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.*
import org.slf4j.LoggerFactory

/**
 * Callback Endpoint for Auth0 signin to retrieve JWT token from code.
 */
@Controller
class SigninController(val properties: AuthProperties,
                       val signinHandler: SigninHandler,
                       val commandDispatcher: CommandDispatcher) : BaseController() {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    /**
     * Sign In Callback Endpoint.
     * Use Code from signin query parameter to retrieve Token from Auth0 and decode and verify it.
     * http://auth.example.test/signin?error=unauthorized&error_description=Access%20denied.
     *
     * @param code
     * @param state
     * @param nonce
     * @param headers
     * @param response
     * @param error
     * @param errorDescription
     */
    @Get("/signin")
    fun signin(
            headers: HttpHeaders,
            @QueryValue("code") code: String?,
            @QueryValue("error") error: String?,
            @QueryValue("error_description") errorDescription: String?,
            @QueryValue("state") state: String?,
            @Header("x-forwarded-host") forwardedHost: String?,
            @CookieValue("AUTH_NONCE") nonce: String?): MutableHttpResponse<Any> {
        printHeaders(headers)
        val command: SigninHandler.SigninCommand = SigninHandler.SigninCommand(forwardedHost, code, error, errorDescription, state, nonce)
        val signinEvent = commandDispatcher.dispatch(signinHandler, command) as SigninHandler.SigninEvent

        return when (signinEvent) {
            is SigninHandler.SigninEvent.SigninComplete -> {
                val response: MutableHttpResponse<Any> = HttpResponse.temporaryRedirect(signinEvent.redirectTo)
                addCookie(response, "ACCESS_TOKEN", signinEvent.accessToken, signinEvent.app.tokenCookieDomain, signinEvent.expiresIn)
                addCookie(response, "JWT_TOKEN", signinEvent.idToken, signinEvent.app.tokenCookieDomain, signinEvent.expiresIn)
                clearCookie(response, "AUTH_NONCE", signinEvent.app.tokenCookieDomain)
                LOGGER.info("SignInSuccessful, redirect to '${signinEvent.redirectTo}'")
                return response
            }
            is SigninHandler.SigninEvent.Error -> throw ApplicationException(signinEvent.reason)
        }
    }
}