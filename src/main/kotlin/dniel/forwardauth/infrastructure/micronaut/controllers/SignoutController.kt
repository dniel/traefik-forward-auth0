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
import dniel.forwardauth.application.commandhandlers.SignoutHandler
import dniel.forwardauth.domain.config.ApplicationSettings
import dniel.forwardauth.infrastructure.micronaut.exceptions.ApplicationException
import dniel.forwardauth.infrastructure.micronaut.security.Auth0Authentication
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.Parameter
import java.net.URI
import org.slf4j.LoggerFactory

/**
 * Sign out controller.
 */
@Controller
@Secured(SecurityRule.IS_AUTHENTICATED)
internal class SignoutController(
        val signoutHandler: SignoutHandler,
        val commandDispatcher: CommandDispatcher
) : BaseController() {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    /**
     * Logout endpoint.
     *
     * @param headers
     * @param response
     */
    @Get("/logout")
    fun logout(@Header("x-forwarded-host") forwardedHost: String,
               @Parameter(hidden = true) authentication: Authentication): MutableHttpResponse<*> {
        val authenticated = (authentication as Auth0Authentication).user
        val command: SignoutHandler.SignoutCommand = SignoutHandler.SignoutCommand(forwardedHost, authenticated.accessToken.toString())
        val signoutEvent = commandDispatcher.dispatch(signoutHandler, command) as SignoutHandler.SignoutEvent

        return when (signoutEvent) {
            is SignoutHandler.SignoutEvent.SignoutComplete -> {
                val noContent = HttpResponse.noContent<Any>()
                clearSessionCookies(signoutEvent.app, noContent)
                noContent
            }
            is SignoutHandler.SignoutEvent.SignoutRedirect -> {
                val redirect: MutableHttpResponse<Any> = HttpResponse.temporaryRedirect(URI(signoutEvent.redirectUrl))
                clearSessionCookies(signoutEvent.app, redirect)
                redirect
            }
            is SignoutHandler.SignoutEvent.Error -> throw ApplicationException(signoutEvent.reason)
        }
    }

    /**
     * Remove session cookies from browser.
     */
    private fun clearSessionCookies(app: ApplicationSettings, response: MutableHttpResponse<*>) {
        LOGGER.debug("Clear session cookies, access token and id token.")
        clearCookie(response, "ACCESS_TOKEN", app.tokenCookieDomain)
        clearCookie(response, "ID_TOKEN", app.tokenCookieDomain)
    }
}