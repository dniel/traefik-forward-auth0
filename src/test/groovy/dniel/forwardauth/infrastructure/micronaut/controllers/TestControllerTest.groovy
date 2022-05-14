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


import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.isA
import static spock.util.matcher.HamcrestSupport.that

/**
 * Unit test for TestController.
 *
 * Uses the declarative annotated client to call
 * the Reactor enabled endpoints of TestController
 * to verify that both the streaming flux endpoint
 * and the mono returning just one item works as
 * expected.
 */
@MicronautTest
class TestControllerTest extends Specification {

    @Client("/")
    static interface TestClient extends TestApi {}

    @Inject
    TestClient testClient

    def "should stream as many records as requested"() {
        when:
        def result = testClient
                .changeEventFlux()
                .take(3)
                .collectList()
                .block()

        then: "we should gotten 3 items"
        that(result.size(), equalTo(3))
    }

    def "should get only one record as requested"() {
        when:
        def result = testClient
                .changeEventMono()
                .block()

        then: "we should get a ProductsChangeEvent"
        that(result, isA(TestController.ChangeEvent))
    }
}
