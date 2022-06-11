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
import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Property
import jakarta.inject.Singleton

/**
 *
 */
@Singleton
@ConfigurationProperties("default")
open class DefaultApplicationSettings : ApplicationSettings {

    override var name = "default"

    @get:JsonIgnore
    override var clientId: String = ""

    @get:JsonIgnore
    override var clientSecret: String = ""

    override var audience: String = ""

    override var scope: String = "profile openid email"

    override var redirectUri: String = ""

    override var tokenCookieDomain: String = ""

    override var restrictedMethods: Array<String> = arrayOf("DELETE", "GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT")

    override var requiredPermissions: Array<String> = emptyArray()

    override var claims: Array<String> = emptyArray()

    override var returnTo: String = ""

    override fun toString(): String {
        return "DefaultApplicationSettings(" +
                "name='$name', " +
                "clientId='$clientId', " +
                "clientSecret='$clientSecret', " +
                "audience='$audience', " +
                "scope='$scope', " +
                "redirectUri='$redirectUri', " +
                "tokenCookieDomain='$tokenCookieDomain', " +
                "restrictedMethods=${restrictedMethods.contentToString()}, " +
                "requiredPermissions=${requiredPermissions.contentToString()}, " +
                "claims=${claims.contentToString()}, " +
                "returnTo='$returnTo')"
    }


}