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
import org.slf4j.LoggerFactory

/**
 * A logging command handler that wrap the dispatch command with log statements.
 */
class LoggingHandler<T : Command>(private val handler: CommandHandler<T>) : CommandHandler<T> by handler {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    override fun handle(params: T): Event {
        val simpleName = handler.javaClass.simpleName
        val start = System.currentTimeMillis()
        val result = handler.handle(params)
        val end = System.currentTimeMillis() - start
        LOGGER.info("Handle Command $simpleName Execution time: $end ms. result=${result.javaClass.simpleName}")
        return result
    }
}