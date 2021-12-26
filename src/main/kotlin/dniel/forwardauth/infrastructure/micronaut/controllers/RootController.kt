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

import dniel.forwardauth.domain.Anonymous
import dniel.forwardauth.domain.Authenticated
import dniel.forwardauth.domain.User
import dniel.forwardauth.infrastructure.micronaut.config.ApplicationConfig
import dniel.forwardauth.infrastructure.siren.Action
import dniel.forwardauth.infrastructure.siren.Link
import dniel.forwardauth.infrastructure.siren.Root
import dniel.forwardauth.infrastructure.siren.Siren
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.ExternalDocumentation
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.slf4j.LoggerFactory
import java.net.URI

@Controller
@Secured(SecurityRule.IS_ANONYMOUS)
internal class RootController(val properties: ApplicationConfig) {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    /**
     * Sign Out endpoint.
     *
     * @param headers
     * @param response
     * @param page is the offset from where to return events
     * @param size is the number of events to return per page
     */
    @Operation(
        tags = arrayOf("start"),
        summary = "Starting point of the application",
        description = "The starting point of the application with hypermedia links is available to available parts " +
            "of the application depenedning of the authorization level of the user.",
        responses = arrayOf(
            ApiResponse(
                responseCode = "200",
                description = "",
                content = arrayOf(
                    Content(
                        schema = Schema(
                            externalDocs = ExternalDocumentation(
                                description = "Link to Siren Hypermedia specification",
                                url = "https://raw.githubusercontent.com/kevinswiber/siren/master/siren.schema.json"
                            )
                        ),
                        mediaType = Siren.APPLICATION_SIREN_JSON
                    )
                )
            )
        )
    )

    @Get("/")
    @Produces(Siren.APPLICATION_SIREN_JSON)
    fun root(@Parameter(hidden = true) authentication: Authentication): HttpResponse<Root> {
        LOGGER.debug("Get root context")

        // TODO
        // FIXME: 25.12.2021 hardcoded anonymous user
        val user: User = Anonymous
        val authorities = authentication.roles

        val links = mutableListOf<Link>()
        val actions = mutableListOf<Action>()

        // when user is already logged in and authenticated, show
        // links available for authenticated users.
        if (user is Authenticated) {
            // add action to signout
            actions += Action(name = "signout", method = "GET", href = URI("/signout"), title = "Signout current user")

            // add link to userinfo
            links += Link(
                type = Siren.APPLICATION_SIREN_JSON,
                clazz = listOf("userinfo"),
                title = "Userinfo for current user",
                rel = listOf("userinfo"),
                href = URI("/userinfo")
            )

            // add link to retrieve application events.
            if (isAdministrator(authorities)) {
                links += Link(
                    type = Siren.APPLICATION_SIREN_JSON,
                    clazz = listOf("event", "collection"),
                    title = "Application events",
                    rel = listOf("events"),
                    href = URI("/events")
                )
            }
        }

        val root = Root.newBuilder()
            .title("ForwardAuth")
            .links(links)
            .actions(actions)
            .build()

        return HttpResponse.ok(root)
    }

    private fun isAdministrator(authorities: Collection<String>) =
        authorities.find { it === "admin:forwardauth" } != null
}