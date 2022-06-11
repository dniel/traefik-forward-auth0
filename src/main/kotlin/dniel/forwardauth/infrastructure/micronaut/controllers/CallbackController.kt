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
import dniel.forwardauth.infrastructure.micronaut.security.Auth0LoginHandler
import io.micronaut.context.annotation.Executable
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.event.ApplicationEvent
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.endpoints.LoginController
import io.micronaut.security.event.LoginFailedEvent
import io.micronaut.security.event.LoginSuccessfulEvent
import io.micronaut.security.rules.SecurityRule
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink

/**
 * # Controller handling Auth0 authorization code callback.
 * @see "https://github.com/micronaut-projects/micronaut-security/blob/master/security-oauth2/src/main/java/io/micronaut/security/oauth2/routes/DefaultOauthController.java"
 */
@Controller
@Replaces(LoginController::class)
@Secured(SecurityRule.IS_ANONYMOUS)
class Auth0CallbackController(
    private val signinHandler: SigninHandler,
    private val commandDispatcher: CommandDispatcher,
    private val authenticationProvider: AuthenticationProvider,
    private val loginHandler: Auth0LoginHandler,
    private val eventPublisher: ApplicationEventPublisher<ApplicationEvent>
) : BaseController() {

    /**
     * ## Login callback from Auth0.
     *
     * TODO rewrite incoming request to ValueObject to represent
     * valid incoming http request parameters to use as login.
     *
     * @param request containing the callback from Auth0.
     * @return a http response containing what to do after a callback.
     */
    @Get("/callback")
    @SingleResult
    @Executable
    fun callback(request: HttpRequest<*>): Publisher<MutableHttpResponse<*>> {
        debug("Received callback request")
        val code = request.parameters["code"]
        val error = request.parameters["error"]
        val errorDescription = request.parameters["error_description"]
        val state = request.parameters["state"]
        val forwardedHost = request.headers["x-forwarded-host"]
        val nonce = request.cookies["AUTH_NONCE"]?.value

        trace("code: $code.")
        trace("error: $error.")
        trace("error description: $errorDescription.")
        trace("state: $state.")
        trace("forwarded host: $forwardedHost.")
        trace("nonce: $nonce.")

        val command: SigninHandler.SigninCommand =
            SigninHandler.SigninCommand(forwardedHost, code, error, errorDescription, state, nonce)
        val signinEvent = commandDispatcher.dispatch(signinHandler, command) as SigninHandler.SigninEvent

        // TODO refactor into LoginHandler
        val authenticationResponse = when (signinEvent) {
            is SigninHandler.SigninEvent.SigninComplete -> {
                Flux.create({ emitter: FluxSink<AuthenticationResponse> ->
                    emitter.next(
                        AuthenticationResponse.success(
                            "user",
                            mapOf(
                                "ACCESS_TOKEN" to signinEvent.accessToken,
                                "ID_TOKEN" to signinEvent.idToken,
                                "REDIRECT_TO" to signinEvent.redirectTo,
                                "COOKIE_DOMAIN" to signinEvent.app.tokenCookieDomain,
                                "EXPIRES_IN" to signinEvent.expiresIn.toLong()
                            )
                        )
                    )

                    emitter.complete()
                }, FluxSink.OverflowStrategy.ERROR)
            }
            is SigninHandler.SigninEvent.Error -> {
                Flux.create({ emitter: FluxSink<AuthenticationResponse> ->
                    emitter.next(AuthenticationResponse.failure(signinEvent.reason))
                    emitter.complete()
                }, FluxSink.OverflowStrategy.ERROR)
            }
        }

        return authenticationResponse
                .map { response ->
                    return@map if (response.isAuthenticated && response.authentication.isPresent) {
                        val authentication: Authentication = response.authentication.get()
                        eventPublisher.publishEvent(LoginSuccessfulEvent(authentication))
                        loginHandler.loginSuccess(authentication, request)
                    } else {
                        eventPublisher.publishEvent(LoginFailedEvent(response))
                        loginHandler.loginFailed(response, request)
                    }
                }.defaultIfEmpty(HttpResponse.status(HttpStatus.UNAUTHORIZED))
    }
}