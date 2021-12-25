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

package dniel.forwardauth.infrastructure.micronaut

import io.micronaut.discovery.event.ServiceReadyEvent
import io.micronaut.runtime.event.annotation.EventListener
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This class listens for the ServiceReadyEvent.
 *
 * It can be used to print debug info of the started application,
 * maybe print some configuration values to the log so that its
 * easy to see important values.
 *
 * Can also be used to initialize the application with data
 * if you need to call out to external systems after the
 * application has started to pre-load cache or similary.
 */
@Singleton
class ApplicationInitializer {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ApplicationInitializer::class.java)
    }

    /**
     * Listen for the application has started even.
     */
    @EventListener
    fun applicationStartedUp(serviceStartedEvent: ServiceReadyEvent) {
        // add something to happen after ServiceReadyEvent here.
    }
}