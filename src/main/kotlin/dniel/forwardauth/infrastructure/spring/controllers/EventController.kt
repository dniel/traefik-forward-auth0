package dniel.forwardauth.infrastructure.spring.controllers

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.domain.events.Event
import dniel.forwardauth.domain.events.EventRepository
import dniel.forwardauth.infrastructure.siren.EmbeddedRepresentation
import dniel.forwardauth.infrastructure.siren.Link
import dniel.forwardauth.infrastructure.siren.Root
import dniel.forwardauth.infrastructure.siren.Siren
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController()
internal class EventController(val properties: AuthProperties, val repo: EventRepository) {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    /**
     * Sign Out endpoint.
     *
     * @param headers
     * @param response
     */
    @PreAuthorize("hasAuthority('admin:forwardauth')")
    @RequestMapping("/events", method = [RequestMethod.GET], produces = [Siren.APPLICATION_SIREN_JSON])
    fun all(): ResponseEntity<Root> {
        LOGGER.trace("Get all events")
        val all = repo.all()
        val countTypes = mutableMapOf<String, Int>()
        countTypes["totalCount"] = all.size
        all.fold(countTypes) { acc, event ->
            acc["${event.type}Count"] = acc.getOrDefault("${event.type}Count", 0) + 1
            acc
        }

        val root = Root.newBuilder()
                .properties(countTypes)
                .links(Link(rel = listOf("self"), href = URI("/events?page")),
                        Link(rel = listOf("next"), href = URI("/events?page?")),
                        Link(rel = listOf("previous"), href = URI("/events?page=")))
                .entities(all.map { event -> createEmbeddedRepresentationEvent(event) })
                .build()

        // TODO: add links
        /*  "links": [
         *       { "rel": [ "self" ], "href": "http://api.x.io/orders/42" },
         *       { "rel": [ "previous" ], "href": "http://api.x.io/orders/41" },
         *       { "rel": [ "next" ], "href": "http://api.x.io/orders/43" }
         *     ]
         */

        return ResponseEntity.ok(root)
    }

    private fun createEmbeddedRepresentationEvent(event: Event): EmbeddedRepresentation {
        return EmbeddedRepresentation.newBuilder(event.id.toString())
                .clazz(event.type)
                .title(event.type)
                .property("id", event.id)
                .property("time", event.time.toString())
                .property("type", event.type)
                .links(Link(rel = listOf("self"), href = URI("/events/${event.id}")))
                .build()
    }
}
