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

package dniel.forwardauth.infrastructure.micronaut.controllers

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.scheduling.annotation.Scheduled
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.util.concurrent.Queues

/* length of random string */
const val STRING_LENGTH = 10;

/**
 * # Interface for the controller endpoints.
 *
 * Used to generate client to communicate with the
 * controller from the Unit Test.
 */
interface TestApi {

    /**
     * ## Return a stream of events.
     *
     * The controller generates one event every second.
     * The annotation @Consumes sets the accept-type
     * on the request so that the client calls the
     * correct endpoint.
     */
    @Get(value = "/changes")
    @Consumes(MediaType.APPLICATION_JSON_STREAM)
    fun changeEventFlux(): Flux<TestController.ChangeEvent>

    /**
     * ## Return a single event.
     */
    @Get(value = "/changes")
    @Consumes(MediaType.APPLICATION_JSON)
    fun changeEventMono(): Mono<TestController.ChangeEvent>
}


/**
 * # Controller that uses Flux and Mono to use Reactor features with Micronaut.
 * The controller expose two endpoints.
 * - changes
 * -
 */
@Controller
@Secured(SecurityRule.IS_ANONYMOUS)
class TestController : TestApi {

    /**
     * Return type from Controller.
     */
    data class ChangeEvent(
            val payload: String,
            val version: Long)

    /* Used as a version count to number the generated updates */
    private val counter = AtomicLong()

    /* the allowed chars to generate random string from, */
    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    /* The sink where new events are broadcasted from. */
    private var sink = Sinks
            .many()
            .multicast()
            .onBackpressureBuffer<ChangeEvent>(
                    Queues.SMALL_BUFFER_SIZE, false);

    /**
     * ## Scheduler to generate new events.
     * If there are any subscribers to the sink, new updates
     * will be added every second.
     */
//    @Scheduled(fixedDelay = "1s")
    fun emitEvents() {
        if (sink.currentSubscriberCount() > 0) {
            val changeEvent = ChangeEvent(randomString(), counter.incrementAndGet())
            println("Emit Event: $changeEvent")
            sink.tryEmitNext(changeEvent)
        } else
            println("do not publish and events, we dont have any subscribers at the moment.")
    }

    /**
     * ## Stream Change Events from sink.
     *
     * This endpoint will send one event every second as long
     * as there are clients connected.
     */
    @Get("/changes")
    @Produces(MediaType.APPLICATION_JSON_STREAM)
    @Secured(SecurityRule.IS_ANONYMOUS)
    override fun changeEventFlux(): Flux<ChangeEvent> {
        return sink.asFlux()
    }


    /**
     * ## Return a single change event.
     */
    @Get("/changes")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured(SecurityRule.IS_ANONYMOUS)
    override fun changeEventMono(): Mono<ChangeEvent> {
        return Mono.just(ChangeEvent(
                randomString(),
                counter.incrementAndGet()))
    }

    /**
     * Generate a random string.
     */
    private fun randomString() = (1..STRING_LENGTH)
            .map { i -> Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
}