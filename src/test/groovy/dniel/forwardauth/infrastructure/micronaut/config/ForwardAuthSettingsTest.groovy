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


import io.micronaut.context.ApplicationContext
import spock.lang.Specification

import static org.hamcrest.Matchers.*
import static spock.util.matcher.HamcrestSupport.that

class ForwardAuthSettingsTest extends Specification {

    def "should read all config config values"() {
        given: "config parameters"
        def map = [
                "domain"           : "1234data",
                "token-endpoint"   : "https://1234data",
                "userinfo-endpoint": "https://1234data",
                "logout-endpoint"  : "https://1234data",
                "authorize-url"    : "https://1234data",
                "nonce-max-age"    : "1234",
                "default.audience" : "default.example.test",
                "apps.0.audience"  : "0.example.test",
                "apps.1.audience"  : "1.example.test",
                "apps.2.audience"  : "2.example.test"
        ]

        when: "we start the application context"
        def ctx = ApplicationContext.run(map)

        then: "the default configuration should be available"
        def settings = ctx.getBean(ForwardAuthSettings.class)
        that(settings.default, is(notNullValue()))
        that(settings.default.name, is(equalTo("default")))
        that(settings.default.audience, is(equalTo("default.example.test")))

        ctx.close()
    }

}