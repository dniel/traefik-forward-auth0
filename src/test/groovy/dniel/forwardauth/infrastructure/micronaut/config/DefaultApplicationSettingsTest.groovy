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

package dniel.forwardauth.infrastructure.micronaut.config

import dniel.forwardauth.domain.config.ApplicationSettings
import io.micronaut.context.ApplicationContext
import io.micronaut.inject.qualifiers.Qualifiers
import spock.lang.Specification

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.notNullValue
import static spock.util.matcher.HamcrestSupport.that

class DefaultApplicationSettingsTest extends Specification {

    def "should read default application values"() {
        given: "config parameters"
        def map = [
                "default.audience": "example.test",
        ]

        when: "we start the application context"
        def ctx = ApplicationContext.run(map)

        then: "the configuration should be available"
        def defaultApp = ctx.getBean(DefaultApplicationSettings.class)

        // that we have one named default.
        that(defaultApp, is(notNullValue()))
        that(defaultApp.name, is("default"))
        that(defaultApp.audience, is("example.test"))

        ctx.close()
    }
}