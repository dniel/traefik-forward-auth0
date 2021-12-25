/*
 * Copyright (c)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dniel.forwardauth.application

import dniel.forwardauth.domain.events.Event
import dniel.forwardauth.domain.events.EventRepository
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

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
@Singleton
class CommandDispatcher(val repo: EventRepository) {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    fun <T : Command> dispatch(handler: CommandHandler<T>, command: T): Event {
        val event = LoggingHandler(handler).handle(command)
        repo.put(event)
        return event
    }
}