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

package dniel.forwardauth.infrastructure.micronaut

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.slf4j.Logger
import org.slf4j.LoggerFactory


@OpenAPIDefinition(
    info = Info(
        title = "ForwardAuth for Auth0 API",
        version = "v3",
        description = "ForwardAuth for Auth0",
        contact = Contact(
            name = "Daniel",
            email = "me@danielnord.no",
            url = "http://github.com/dniel"
        )
    )
)
@SecurityScheme(
    name = "forwardauth",
    paramName = "ACCESS_TOKEN",
    type = SecuritySchemeType.OAUTH2,
    `in` = SecuritySchemeIn.COOKIE,
    bearerFormat = "jwt"
)
object Application {
    private val log: Logger = LoggerFactory.getLogger(Application::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
            .packages("dniel.forwardauth")
            .mainClass(Application.javaClass)
            .start()
    }
}