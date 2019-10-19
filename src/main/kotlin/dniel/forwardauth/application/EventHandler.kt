package dniel.forwardauth.application

import dniel.forwardauth.domain.events.Event

interface EventHandler<in T : Event> {
    fun handle(params: T): Event
}