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

    def "should read list of default application values"() {
        given: "config parameters"
        def map = [
                "apps.default.name": "default.example.test",
                "apps.0.name"      : "0.example.test",
                "apps.1.name"      : "1.example.test",
                "apps.2.name"      : "2.example.test"
        ]

        when: "we start the application context"
        def ctx = ApplicationContext.run(map)

        then: "the configuration should be available"
        def defaultlist = ctx.getBeansOfType(ApplicationSettings.class)
        def defaultApp = ctx.getBean(
                ApplicationSettings.class, Qualifiers.byName("default"))


        // assert that we have all apps in configuration.
        that(map.size(), is(equalTo(defaultlist.size())))
        that(defaultlist, is(notNullValue()))

        // that we have one named default.
        that(defaultApp, is(notNullValue()))
        that(defaultApp.name, is("default.example.test"))

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
                "apps.default.name": "default.example.test",
                "apps.0.name"      : "0.example.test",
                "apps.1.name"      : "1.example.test",
                "apps.2.name"      : "2.example.test"
        ]

        when: "we start the application context"
        def ctx = ApplicationContext.run(map)

        then: "the configuration should be available"
        def defaultlist = ctx.getBeansOfType(ApplicationSettings.class)
        def defaultApp = ctx.getBean(ApplicationSettings.class, Qualifiers.byName("default"))

        that(defaultApp.name, is(equalTo("default.example.test")))
        that(defaultApp, is(notNullValue()))
        that(defaultlist, is(notNullValue()))

        ctx.close()
    }
}