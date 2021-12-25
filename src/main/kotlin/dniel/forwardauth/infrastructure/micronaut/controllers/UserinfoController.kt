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
import dniel.forwardauth.application.commandhandlers.UserinfoHandler
import dniel.forwardauth.domain.Anonymous
import dniel.forwardauth.infrastructure.siren.Root
import dniel.forwardauth.infrastructure.siren.Siren.APPLICATION_SIREN_JSON
import dniel.forwardauth.domain.exceptions.ApplicationException
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.*
import io.swagger.v3.oas.annotations.ExternalDocumentation
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.slf4j.LoggerFactory

@Controller
@Secured(IS_AUTHENTICATED)
internal class UserinfoController(val userinfoHandler: UserinfoHandler,
                                  val commandDispatcher: CommandDispatcher) {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    /**
     * Userinfo endpoint.
     *
     * @param headers
     * @param response
     */
    @Operation(
            tags = arrayOf("userinfo"),
            summary = "Get userinfo",
            description = "Get userinfo of authenticated user.",
            responses = arrayOf(
                    ApiResponse(
                            responseCode = "200",
                            description = "Userinfo about the currently authenticated user.",
                            content = arrayOf(
                                    Content(
                                            schema = Schema(
                                                    externalDocs = ExternalDocumentation(
                                                            description = "Link to Siren Hypermedia specification",
                                                            url = "https://raw.githubusercontent.com/kevinswiber/siren/master/siren.schema.json")),
                                            mediaType = APPLICATION_SIREN_JSON))
                    ),
                    ApiResponse(
                            responseCode = "401",
                            description = "If no authenticated user.",
                            content = arrayOf(Content())
                    )
            ))
    @Get("/userinfo")
    @Secured(IS_AUTHENTICATED)
    @Produces(APPLICATION_SIREN_JSON)
    fun userinfo(@Parameter(hidden = true) authentication: Authentication): HttpResponse<Root> {
        // TODO
        // FIXME: 25.12.2021 hardcoded anonymous user.
        val authenticated = Anonymous

        // get userinfo
        val command: UserinfoHandler.UserinfoCommand = UserinfoHandler.UserinfoCommand(authenticated)
        val userinfoEvent = commandDispatcher.dispatch(userinfoHandler, command) as UserinfoHandler.UserinfoEvent

        return when (userinfoEvent) {
            is UserinfoHandler.UserinfoEvent.Userinfo -> {
                val root = Root.newBuilder()
                        .title("Userinfo for ${authentication.name}")
                        .properties(userinfoEvent.properties)
                        .clazz("userinfo")
                        .build()
                HttpResponse.ok(root)
            }
            is UserinfoHandler.UserinfoEvent.Error -> throw ApplicationException(userinfoEvent.reason)
        }
    }
}
