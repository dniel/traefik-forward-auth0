package dniel.forwardauth.infrastructure.spring.controllers

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.domain.events.Event
import dniel.forwardauth.domain.events.EventRepository
import dniel.forwardauth.infrastructure.siren.EmbeddedRepresentation
import dniel.forwardauth.infrastructure.siren.Link
import dniel.forwardauth.infrastructure.siren.Root
import dniel.forwardauth.infrastructure.siren.Siren
import io.swagger.v3.oas.annotations.ExternalDocumentation
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import dniel.forwardauth.infrastructure.siren.*
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.*

@RestController
internal class EventController(val properties: AuthProperties, val repo: EventRepository) {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    /**
     * Get all events
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
    @PreAuthorize("hasAuthority('admin:forwardauth')")
    @RequestMapping("/events",
            method = [RequestMethod.GET],
            produces = [Siren.APPLICATION_SIREN_JSON])
    fun all(@Parameter(description = "Page to retrieve, default page 0", required = false, `in` = ParameterIn.QUERY)
            @RequestParam("page", defaultValue = "0", required = false) page: Int,
            @Parameter(description = "Size of page, default size 20", required = false, `in` = ParameterIn.QUERY)
            @RequestParam("size", defaultValue = "20", required = false) size: Int): ResponseEntity<Root> {
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

        // navigational links
        val links = mutableListOf<Link>()

        // Link back to start page
        links += Link(type = Siren.APPLICATION_SIREN_JSON, clazz = listOf("root"), title = "start", rel = listOf("start"), href = URI("/"))

        links += Link(type = Siren.APPLICATION_SIREN_JSON, clazz = listOf("event", "collection"),
                title = "Current page", rel = listOf("current"), href = URI("/events?page=$page&size=$size"))
        if (prevPage != page) links += Link(type = Siren.APPLICATION_SIREN_JSON, clazz = listOf("event", "collection"),
                title = "Previous page", rel = listOf("prev"), href = URI("/events?page=$prevPage&size=$size"))
        if (nextPage != page) links += Link(type = Siren.APPLICATION_SIREN_JSON, clazz = listOf("event", "collection"),
                title = "Next page", rel = listOf("next"), href = URI("/events?page=$nextPage&size=$size"))


        // add action to signout
        val actions = listOf(Action(title = "Clear Events", name = "clear", method = "DELETE", href = URI("/events")))

        val subList = all.slice(startIndex..endIndex)
        val root = Root.newBuilder()
                .title("Events")
                .clazz("event", "collection")
                .properties(countTypes)
                .links(links)
                .actions(actions)
                .entities(subList.map { event -> createEmbeddedRepresentationEvent(event) })
                .build()

        return ResponseEntity.ok(root)
    }

    /**
     * Get all events
     *
     * @param headers
     * @param response
     * @param page is the offset from where to return events
     * @param size is the number of events to return per page
     */
    @PreAuthorize("hasAuthority('admin:forwardauth')")
    @RequestMapping("/events/{id}",
            method = [RequestMethod.GET],
            produces = [Siren.APPLICATION_SIREN_JSON])
    fun one(@PathVariable("id") id: String): ResponseEntity<Any> {
        val one = repo.get(UUID.fromString(id))
        return if (one == null) {
            ResponseEntity.notFound().build()
        } else {
            // root page
            val links = listOf(
                    Link(type = Siren.APPLICATION_SIREN_JSON, clazz = listOf("event", "collection"), title = "Up", rel = listOf("up"), href = URI("/events")),
                    Link(type = Siren.APPLICATION_SIREN_JSON, clazz = listOf("root"), title = "Start", rel = listOf("start"), href = URI("/")))

            val root = Root.newBuilder()
                    .title("Event")
                    .clazz("event")
                    .links(links)
                    .entities(listOf(createEmbeddedRepresentationEvent(one)))
                    .build()
            ResponseEntity.ok(root)
        }
    }

    /**
     * Delete all events
     */
    @PreAuthorize("hasAuthority('admin:forwardauth')")
    @RequestMapping("/events",
            method = [RequestMethod.DELETE])
    fun clear(): ResponseEntity<Any> {
        repo.clear()
        return ResponseEntity.ok().build()
    }


    private fun createEmbeddedRepresentationEvent(event: Event): EmbeddedRepresentation {
        return EmbeddedRepresentation.newBuilder(event.id.toString())
                .clazz(event.type)
                .title("${event.type} ${event.id}")
                .property("id", event.id)
                .property("time", event.time.toString())
                .property("type", event.type)
                .links(Link(
                        type = Siren.APPLICATION_SIREN_JSON,
                        clazz = listOf("event"),
                        title = "${event.type} event ${event.id}",
                        rel = listOf("item"),
                        href = URI("/events/${event.id}")))
                .build()
    }
}
