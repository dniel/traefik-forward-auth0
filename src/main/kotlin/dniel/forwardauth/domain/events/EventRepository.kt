package dniel.forwardauth.domain.events

import com.google.common.cache.CacheBuilder
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Simple in-memory repository to hold events.
 * It expires the entries 24 hours after they have been written to the cache to
 * avoid memory usage to build up.
 * <p/>
 * TODO: implement more advanced repository and/or configuration for events.
 */
@Component()
class EventRepository{

    private val cache = CacheBuilder.newBuilder().expireAfterWrite(24, TimeUnit.HOURS).build<UUID, Event>()

    fun all(): List<Event> {
        return cache.asMap().values.toList()
    }

    fun put(event: Event) {
        cache.put(event.id, event)
    }
}