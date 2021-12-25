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

package dniel.forwardauth.domain.events

import com.google.common.cache.CacheBuilder
import jakarta.inject.Singleton
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Simple in-memory repository to hold events.
 * It expires the entries 24 hours after they have been written to the cache to
 * avoid memory usage to build up.
 * <p/>
 * TODO: implement more advanced repository and/or configuration for events.
 */
@Singleton
class EventRepository{

    private val cache = CacheBuilder.newBuilder().expireAfterWrite(24, TimeUnit.HOURS).build<UUID, Event>()

    fun all(): List<Event> {
        return cache.asMap().values.toList()
    }

    fun put(event: Event) {
        cache.put(event.id, event)
    }
}