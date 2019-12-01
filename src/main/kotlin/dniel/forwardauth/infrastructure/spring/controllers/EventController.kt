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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import javax.servlet.http.HttpServletResponse

@RestController()
internal class EventController(val properties: AuthProperties, val repo: EventRepository) {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    /**
     * Sign Out endpoint.
     *
     * @param headers
     * @param response
     * @param page is the offset from where to return events
     * @param size is the number of events to return per page
     */
    @PreAuthorize("hasAuthority('admin:forwardauth')")
    @RequestMapping("/events",
            method = [RequestMethod.GET],
            produces = [Siren.APPLICATION_SIREN_JSON])
    fun all(@RequestParam("page", defaultValue = "0", required = false) page: Int,
            @RequestParam("size", defaultValue = "20", required = false) size: Int): ResponseEntity<Root> {
        LOGGER.debug("Get all events page=$page, size=$size")
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

        val links = mutableListOf<Link>(Link(type = Siren.APPLICATION_SIREN_JSON, clazz = listOf("event", "collection"),
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

        return ResponseEntity.ok(root)
    }

    private fun createEmbeddedRepresentationEvent(event: Event): EmbeddedRepresentation {
        return EmbeddedRepresentation.newBuilder(event.id.toString())
                .clazz(event.type)
                .title("${event.type} event ${event.id}")
                .property("id", event.id)
                .property("time", event.time.toString())
                .property("type", event.type)
                .links(Link(type = Siren.APPLICATION_SIREN_JSON, clazz = listOf("event"),
                        title = "${event.type} event ${event.id}", rel = listOf("self"), href = URI("/events/${event.id}")))
                .build()
    }
}
