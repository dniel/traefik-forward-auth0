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
import dniel.forwardauth.domain.Authenticated
import dniel.forwardauth.domain.User
import dniel.forwardauth.infrastructure.micronaut.security.Auth0Authentication
import dniel.forwardauth.infrastructure.siren.EmbeddedRepresentation
import dniel.forwardauth.infrastructure.siren.Link
import dniel.forwardauth.infrastructure.siren.Root
import dniel.forwardauth.infrastructure.siren.Siren.APPLICATION_SIREN_JSON
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.ok
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
import java.net.URI
import org.slf4j.LoggerFactory

@Controller
internal class UserinfoController(
        val userinfoHandler: UserinfoHandler,
        val commandDispatcher: CommandDispatcher
) {

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
                                                            url = "https://raw.githubusercontent.com/kevinswiber/siren/master/siren.schema.json"
                                                    )
                                            ),
                                            mediaType = APPLICATION_SIREN_JSON
                                    )
                            )
                    ),
                    ApiResponse(
                            responseCode = "401",
                            description = "If no authenticated user.",
                            content = arrayOf(Content())
                    )
            )
    )
    @Get("/userinfo")
    @Secured(IS_AUTHENTICATED)
    @Produces(APPLICATION_SIREN_JSON)
    fun userinfo(@Parameter(hidden = true) authentication: Authentication): HttpResponse<Root> {
        val user = (authentication as Auth0Authentication).user
        val properties = mutableMapOf<String, Any>()
        properties["permissions"] = user.permissions.joinToString()
        properties["is_authenticated"] = user is Authenticated

        // create siren response object.
        val root = Root.newBuilder()
                .title("Userinfo for ${authentication.name}")
                .properties(properties)
                .entities(createEmbeddedRepresentationUser(user))
                .clazz("userinfo")

        return ok(root.build())
    }

    private fun createEmbeddedRepresentationUser(user: User): EmbeddedRepresentation {
        return EmbeddedRepresentation.newBuilder("http://x.io/rels/user")
                .clazz(listOf(user.javaClass.simpleName, "User"))
                .title("$user")
                .properties(user.userinfo)
                .links(
                        Link(
                                type = APPLICATION_SIREN_JSON,
                                clazz = listOf("User"),
                                title = "$user",
                                rel = listOf("self"),
                                href = URI("/userinfo")
                        )
                )
                .build()
    }
}