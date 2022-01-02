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
import dniel.forwardauth.application.commandhandlers.AuthorizeHandler
import dniel.forwardauth.domain.Anonymous
import dniel.forwardauth.domain.Authenticated
import dniel.forwardauth.domain.User
import dniel.forwardauth.infrastructure.micronaut.config.ForwardAuthSettings
import dniel.forwardauth.infrastructure.micronaut.exceptions.AuthorizationException
import dniel.forwardauth.infrastructure.micronaut.exceptions.PermissionDeniedException
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_ANONYMOUS
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.inject.Inject
import org.slf4j.LoggerFactory

/**
 * Authorize Endpoint.
 * This endpoint is used by ForwardAuth to authorize requests.
 */
@Controller
@Secured(IS_ANONYMOUS)
class AuthorizeController(
        @Inject private val authorizeHandler: AuthorizeHandler,
        @Inject private val commandDispatcher: CommandDispatcher,
        @Inject private val authProperties: ForwardAuthSettings
) : BaseController() {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    /**
     * Authorize Endpoint
     */
    @Operation(
            summary = "Authorize requests.",
            description = "This endpoint is called by Traefik to check if a request is authorized to access.",
            responses = arrayOf(
                    ApiResponse(
                            responseCode = "200",
                            description = "Access granted according to configuration in ForwardAuth and Auth0.",
                            content = arrayOf(Content())
                    ),
                    ApiResponse(
                            responseCode = "401",
                            description = "Access denied according to configuration in ForwardAuth and Auth0."
                    ),
                    ApiResponse(
                            responseCode = "307",
                            description = "Redirect for authentication with Auth0",
                            content = arrayOf(Content())
                    )
            )
    )
    @Get("/authorize")
    @Secured(IS_ANONYMOUS)
    fun authorize(
            authorizeRequest: AuthorizeRequest,
            authentication: Authentication?,
            @Header("Accept") acceptContentHeader: String?,
            @Header("x-requested-with") requestedWithHeader: String?,
    ): MutableHttpResponse<Any> {
        return authenticateToken(
                acceptContent = acceptContentHeader,
                requestedWith = requestedWithHeader,
                host = authorizeRequest.forwardedHost,
                method = authorizeRequest.forwardedHost,
                protocol = authorizeRequest.forwardedHost,
                uri = authorizeRequest.forwardedHost,
                user = Anonymous)
    }

    /**
     * Authenticate
     *
     */
    private fun authenticateToken(acceptContent: String?, requestedWith: String?, method: String, host: String, protocol: String,
                                  uri: String, user: User): MutableHttpResponse<Any> {
        val authorizeResult = handleCommand(acceptContent, requestedWith, protocol, host, uri, method, user)
        return when (authorizeResult) {
            is AuthorizeHandler.AuthorizeEvent.AccessDenied -> throw PermissionDeniedException(authorizeResult)
            is AuthorizeHandler.AuthorizeEvent.Error -> throw AuthorizationException(authorizeResult)
            is AuthorizeHandler.AuthorizeEvent.NeedRedirect -> {
                redirect(authorizeResult)
            }
            is AuthorizeHandler.AuthorizeEvent.AccessGranted -> {
                accessGranted(authorizeResult)
            }
        }
    }

    /**
     * Execute AuthorizeCommand
     *
     */
    private fun handleCommand(acceptContent: String?, requestedWithHeader: String?,
                              protocol: String, host: String, uri: String, method: String, user: User): AuthorizeHandler.AuthorizeEvent {
        val isApi = (acceptsApiContent(acceptContent)) ||
                requestedWithHeader != null && requestedWithHeader == "XMLHttpRequest"

        val command: AuthorizeHandler.AuthorizeCommand = AuthorizeHandler.AuthorizeCommand(user, protocol, host, uri, method, isApi)
        return commandDispatcher.dispatch(authorizeHandler, command) as AuthorizeHandler.AuthorizeEvent
    }

    /**
     * Access Granted.
     * When user is Authenticated, add userinfo to headers to
     * forward userinfo from tokens to requested resource server.
     * When user is Anonymous just and access granted, just say 200 with no userinfo.
     */
    private fun accessGranted(authorizeResult: AuthorizeHandler.AuthorizeEvent.AccessGranted): MutableHttpResponse<Any>  {
        LOGGER.debug("Access Granted to ${authorizeResult.user}")
        return when (authorizeResult.user) {
            is Authenticated -> {
                val response = HttpResponse.noContent<Any>()
                val accessToken = authorizeResult.user.accessToken.raw
                response.header("Authorization", "Bearer ${accessToken}")
                authorizeResult.user.userinfo.forEach { k, v ->
                    val headerName = "x-forwardauth-${k.replace('_', '-')}".lowercase()
                    LOGGER.trace("Add header ${headerName} with value ${v}")
                    response.header(headerName, v)
                }
                response
            }
            else -> HttpResponse.noContent()
        }
    }

    /**
     * Redirect to Authorize
     *
     */
    private fun redirect(authorizeResult: AuthorizeHandler.AuthorizeEvent.NeedRedirect): MutableHttpResponse<Any>  {
        LOGGER.debug("Redirect to ${authorizeResult.authorizeUrl}")
        val response = HttpResponse.temporaryRedirect<Any>(authorizeResult.authorizeUrl)

        // add the nonce value to the request to be able to retrieve ut again on the singin endpoint.
        addCookie(response,"AUTH_NONCE", authorizeResult.nonce.value, authorizeResult.cookieDomain, authProperties.nonceMaxAge)
        return response
    }

    private fun acceptsApiContent(acceptContent: String?) =
            acceptContent != null &&
                    (acceptContent.contains("application/json") || acceptContent.contains("text/event-stream"))
}