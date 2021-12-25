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

import dniel.forwardauth.domain.events.Event
import dniel.forwardauth.domain.events.EventRepository
import dniel.forwardauth.infrastructure.micronaut.config.ApplicationConfig
import dniel.forwardauth.infrastructure.siren.EmbeddedRepresentation
import dniel.forwardauth.infrastructure.siren.Link
import dniel.forwardauth.infrastructure.siren.Root
import dniel.forwardauth.infrastructure.siren.Siren
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.ok
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.swagger.v3.oas.annotations.ExternalDocumentation
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import java.net.URI
import org.slf4j.LoggerFactory

@Controller
internal class EventController(val properties: ApplicationConfig, val repo: EventRepository) {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    /**
     * Events endpoint.
     *
     * @param headers
     * @param response
     * @param page is the offset from where to return events
     * @param size is the number of events to return per page
     */
    @Operation(
            tags = arrayOf("events"),
            summary = "Get Events",
            description = "Retrieve application events, contains information about events that has happened and how many of them.",
            security = arrayOf(SecurityRequirement(
                    name = "forwardauth",
                    scopes = arrayOf("admin:forwardauth"))),
            responses = arrayOf(
                    ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved a page of events.",
                            content = arrayOf(
                                    Content(
                                            schema = Schema(
                                                    externalDocs = ExternalDocumentation(
                                                            description = "Link to Siren Hypermedia specification",
                                                            url = "https://raw.githubusercontent.com/kevinswiber/siren/master/siren.schema.json")),
                                            mediaType = Siren.APPLICATION_SIREN_JSON))
                    ),
                    ApiResponse(
                            responseCode = "404",
                            description = "Page of events does not exist.",
                            content = arrayOf(Content(mediaType = Siren.APPLICATION_SIREN_JSON))
                    )
            )
    )

    @Secured("admin:forwardauth")
    @Get("/events")
    @Produces(Siren.APPLICATION_SIREN_JSON)
    fun all(
            @QueryValue
            @Parameter(description = "Page to retrieve, default page 0",
                    required = false,
                    `in` = ParameterIn.QUERY) page: Int,
            @QueryValue
            @Parameter(
                    description = "Size of page, default size 20",
                    required = false,
                    `in` = ParameterIn.QUERY) size: Int): HttpResponse<Root> {

        LOGGER.debug("Get Event page=$page, size=$size")
        val all = repo.all()
        val countTypes = mutableMapOf<String, Int>()
        countTypes["totalCount"] = all.size
        all.fold(countTypes) { acc, event ->
            acc["${event.type}Count"] = acc.getOrDefault("${event.type}Count", 0) + 1
            acc
        }

        val prevPage = if (page > 0) page - 1 else page
        val nextPage = if (page + 1 * size < all.size) page + 1 else page
        val startIndex = page * size
        val endIndex = if (nextPage * size > all.size) all.size else nextPage * size

        val links = mutableListOf(Link(type = Siren.APPLICATION_SIREN_JSON, clazz = listOf("event", "collection"),
                title = "Current page", rel = listOf("self"), href = URI("/events?page=$page&size=$size")))
        if (prevPage != page) links += Link(type = Siren.APPLICATION_SIREN_JSON, clazz = listOf("event", "collection"),
                title = "Previous page", rel = listOf("previous"), href = URI("/events?page=$prevPage&size=$size"))
        if (nextPage != page) links += Link(type = Siren.APPLICATION_SIREN_JSON, clazz = listOf("event", "collection"),
                title = "Next page", rel = listOf("next"), href = URI("/events?page=$nextPage&size=$size"))

        val subList = all.slice(startIndex..endIndex)
        val root = Root.newBuilder()
                .title("Events")
                .clazz("event", "collection")
                .properties(countTypes)
                .links(links)
                .entities(subList.map { event -> createEmbeddedRepresentationEvent(event) })
                .build()

        return ok(root)
    }

    private fun createEmbeddedRepresentationEvent(event: Event): EmbeddedRepresentation {
        return EmbeddedRepresentation.newBuilder(event.id.toString())
                .clazz(event.type)
                .title("${event.type} event ${event.id}")
                .property("id", event.id)
                .property("time", event.time.toString())
                .property("type", event.type)
                .links(Link(
                        type = Siren.APPLICATION_SIREN_JSON,
                        clazz = listOf("event"),
                        title = "${event.type} event ${event.id}",
                        rel = listOf("self"),
                        href = URI("/events/${event.id}")))
                .build()
    }
}
