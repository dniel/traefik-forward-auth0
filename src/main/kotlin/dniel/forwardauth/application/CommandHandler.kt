package dniel.forwardauth.application

import dniel.forwardauth.domain.events.Event

interface Command {
}

interface CommandHandler<in T : Command> {
    fun handle(params: T): Event
}