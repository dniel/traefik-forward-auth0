package dniel.forwardauth.application

import dniel.forwardauth.domain.events.Event
import dniel.forwardauth.domain.events.EventRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Generic command dispatcher.
 * <p/>
 * Central component to dispatch commands, a good place to add cross-cutting converns to commands.
 * For example:
 * <li>logging
 * <li>security
 * <li>tracing
 *
 * <p/>
 * TODO:
 * Should probably also create the commandhandler instances so that they are not passed in as
 * parameter by the caller.
 */
@Component
class CommandDispatcher(val repo: EventRepository) {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    fun <T : Command> dispatch(handler: CommandHandler<T>, command: T): Event {
        val event = LoggingHandler(handler).handle(command)
        repo.put(event)
        return event
    }
}