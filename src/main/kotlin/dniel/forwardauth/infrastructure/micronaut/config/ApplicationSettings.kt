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

import com.fasterxml.jackson.annotation.JsonIgnore
import dniel.forwardauth.domain.config.ApplicationSettings
import io.micronaut.context.annotation.EachProperty
import io.micronaut.context.annotation.Parameter
import io.micronaut.context.annotation.Value
import io.micronaut.core.annotation.Introspected
import jakarta.inject.Singleton

/**
 *
 */
@Singleton
@EachProperty(value = "apps")
@Introspected
class ApplicationSettings(@Parameter override var name: String) : ApplicationSettings {

    @Value("\${client-id}")
    @get:JsonIgnore
    override var clientId: String = ""

    @Value("\${client-secret}")
    @get:JsonIgnore
    override var clientSecret: String = ""

    @Value("\${audience}")
    override var audience: String = ""

    @Value("\${scope}")
    override var scope: String = "profile openid email"

    @Value("\${redirect-uri}")
    override var redirectUri: String = ""

    @Value("\${token-cookie-domain}")
    override var tokenCookieDomain: String = ""

    @Value("\${restricted-methods}")
    override var restrictedMethods: Array<String> = arrayOf("DELETE", "GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT")

    @Value("\${required-permissions}")
    override var requiredPermissions: Array<String> = emptyArray()

    @Value("\${claims}")
    override var claims: Array<String> = emptyArray()

    @Value("\${return-to}")
    override var returnTo: String = ""


    override fun toString(): String {
        return "ApplicationSettings(" +
                "name='$name', audience='$audience', scope='$scope', redirectUri='$redirectUri', " +
                "tokenCookieDomain='$tokenCookieDomain', " +
                "restrictedMethods=${restrictedMethods.contentToString()}, " +
                "requiredPermissions=${requiredPermissions.contentToString()}, " +
                "claims=${claims.contentToString()}, returnTo='$returnTo')"
    }
}