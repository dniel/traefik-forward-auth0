package dniel.forwardauth.infrastructure.spring.controllers

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.domain.events.Event
import dniel.forwardauth.domain.events.EventRepository
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

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
    @RequestMapping("/events", method = [RequestMethod.GET], produces = ["application/json"])
    fun all(): ResponseEntity<Collection<Event>> {
        LOGGER.trace("Get all events")
        return ResponseEntity.ok(repo.all())
    }
}
