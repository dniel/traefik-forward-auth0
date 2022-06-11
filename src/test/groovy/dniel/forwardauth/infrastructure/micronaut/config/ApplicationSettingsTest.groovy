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

class ApplicationSettingsTest extends Specification {

    def "should read list of application values"() {
        given: "config parameters"
        def map = [
                "apps.0.name": "0.example.test",
                "apps.1.name": "1.example.test",
                "apps.2.name": "2.example.test"
        ]

        when: "we start the application context"
        def ctx = ApplicationContext.run(map)

        then: "the configuration should be available"
        def apps = ctx.getBeansOfType(ApplicationSettings.class)
        apps.count { it.name == "0.example.test" } == 1
        apps.count { it.name == "1.example.test" } == 1
        apps.count { it.name == "2.example.test" } == 1

        ctx.close()
    }

    def "should read all config config values"() {
        given: "config parameters"
        def map = [
                "domain"           : "1234data",
                "token-endpoint"   : "https://1234data",
                "userinfo-endpoint": "https://1234data",
                "logout-endpoint"  : "https://1234data",
                "authorize-url"    : "https://1234data",
                "nonce-max-age"    : "1234",
                "apps.0.name"      : "0.example.test",
                "apps.1.name"      : "1.example.test",
                "apps.2.name"      : "2.example.test"
        ]

        when: "we start the application context"
        def ctx = ApplicationContext.run(map)

        then: "the configuration should be available"
        def apps = ctx.getBeansOfType(ApplicationSettings.class)
        def defaultApp = ctx.getBean(DefaultApplicationSettings.class)

        that(defaultApp.name, is(equalTo("default")))
        that(defaultApp, is(notNullValue()))
        that(apps, is(notNullValue()))

        ctx.close()
    }
}