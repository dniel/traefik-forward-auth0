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
import dniel.forwardauth.infrastructure.micronaut.config.AuthProperties
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.security.authentication.Authentication
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.slf4j.LoggerFactory


/**
 * Authorize Endpoint.
 * This endpoint is used by ForwardAuth to authorize requests.
 */
@Controller
class AuthorizeController(val authorizeHandler: AuthorizeHandler,
                          val commandDispatcher: CommandDispatcher,
                          val authProperties: AuthProperties) : BaseController() {

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
    fun authorize(
            authentication: Authentication,
            headers: HttpHeaders,
            @Header("Accept") acceptContent: String?,
            @Header("x-requested-with") requestedWithHeader: String?,
            @Header("x-forwarded-host") forwardedHostHeader: String,
            @Header("x-forwarded-proto") forwardedProtoHeader: String,
            @Header("x-forwarded-uri") forwardedUriHeader: String,
            @Header("x-forwarded-method") forwardedMethodHeader: String): MutableHttpResponse<Any> {
        return HttpResponse.ok()
    }
}
