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
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Ignore
import spock.lang.Specification

import static org.hamcrest.Matchers.equalTo
import static spock.util.matcher.HamcrestSupport.that

@MicronautTest
class AuthorizeControllerTest extends Specification {

    @Inject
    @Client("/")
    HttpClient client

    @Ignore("write a working test that actually test something")
    def "should test the authenticator"() {
        given: "some input"

        when: "we do something with the authenticator"
        def httpRequest = HttpRequest.GET('/authorize')
        httpRequest.header("x-forwarded-host","https://blablabla.com")
        httpRequest.header("x-forwarded-proto","https")
        httpRequest.header("x-forwarded-method","GET")
        httpRequest.header("x-forwarded-uri","/something/something")
        def result = client.toBlocking().exchange(httpRequest)

        then: "we should verify the outcome"
        that(result.status, equalTo(HttpStatus.NO_CONTENT))
    }
}
