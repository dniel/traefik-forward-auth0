package dniel.forwardauth.domain.events

import dniel.forwardauth.domain.shared.User
import java.time.LocalDateTime
import java.util.*

abstract class Event(val user: User) {

    val id: UUID
    val time: LocalDateTime
    val type: String

    init {
        time = LocalDateTime.now()
        id = UUID.randomUUID()
        type = this::class.simpleName ?: "unknown"
    }
}