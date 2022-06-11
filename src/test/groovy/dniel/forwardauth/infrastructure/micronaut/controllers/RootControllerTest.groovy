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
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Shared
import spock.lang.Specification

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.notNullValue
import static spock.util.matcher.HamcrestSupport.that

@MicronautTest
class RootControllerTest extends Specification {

    @Inject
    @Client('/')
    @Shared
    HttpClient client

    def "should allow anonymous to access to root"() {
        when: "we call as anonymous user to root"
        def httpRequest = HttpRequest.GET('/')
        def result = client.toBlocking().exchange(httpRequest)

        then: "we should get status OK"
        that(result.status, equalTo(HttpStatus.OK))
    }

    def "should return siren json from root"() {
        when: "we call as anonymous user to root"
        def httpRequest = HttpRequest.GET('/')
        def result = client.toBlocking().exchange(httpRequest)

        then: "we should get siren content back"
        that(result.contentType.isPresent(), equalTo(Boolean.TRUE))
        that(result.contentType.get(), equalTo(MediaType.of("application/vnd.siren+json")))
    }
}
