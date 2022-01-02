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

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

import static org.hamcrest.Matchers.equalTo
import static spock.util.matcher.HamcrestSupport.that

@MicronautTest
class EventsControllerTest extends Specification {

    @Inject
    @Client('/')
    HttpClient client

    def "should require authentication to access events"() {
        when: "we call as anonymous user to events"
        def httpRequest = HttpRequest.GET('/events')
        def result = client.toBlocking().exchange(httpRequest)

        then: "we should get permission denied"
        def ex = thrown(HttpClientResponseException)
        that(ex.message, equalTo("Unauthorized"))
        that(ex.status, equalTo(HttpStatus.UNAUTHORIZED))
    }
}
