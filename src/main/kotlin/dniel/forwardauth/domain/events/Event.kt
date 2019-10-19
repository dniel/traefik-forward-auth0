package dniel.forwardauth.domain.events

import java.time.LocalDateTime
import java.util.*

abstract class Event() {

    val id: UUID
    val time: LocalDateTime
    val type: String

    init {
        time = LocalDateTime.now()
        id = UUID.randomUUID()
        type = this::class.simpleName ?: "unknown"
    }
}