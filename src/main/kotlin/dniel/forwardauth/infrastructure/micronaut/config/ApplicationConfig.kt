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

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.EachProperty
import io.micronaut.context.annotation.Value
import io.micronaut.core.annotation.Introspected
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.validation.constraints.Min
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Singleton
class ApplicationConfig {

    @Value("\${domain}")
    @NotEmpty
    lateinit var domain: String

    @Value("\${token-endpoint}")
    @NotEmpty
    lateinit var tokenEndpoint: String

    @Value("\${logout-endpoint}")
    @NotEmpty
    lateinit var logoutEndpoint: String

    @Value("\${userinfo-endpoint}")
    @NotEmpty
    lateinit var userinfoEndpoint: String

    @Value("\${authorize-url}")
    @NotEmpty
    lateinit var authorizeUrl: String

    @Value("\${nonce-max-age}")
    @Min(-1)
    var nonceMaxAge: Long = 60

    @Value("\${default}")
    @NotNull
    val default = Application()

    @Inject
    @NotNull
    lateinit var test: List<DefaultApplication>

    @ConfigurationProperties("apps")
    @Introspected
    @EachProperty("apps")
    class DefaultApplication {
        @Value("\${name}")
        @NotEmpty
        lateinit var name: String

        override fun toString(): String {
            return "DefaultApplication(name='$name')"
        }
    }

    val apps = ArrayList<Application>()

    override fun toString(): String {
        return "AuthProperties(test='$test', domain='$domain', tokenEndpoint='$tokenEndpoint', authorizeUrl='$authorizeUrl', default=$default, apps=$apps)"
    }

    /**
     * Return application with application specific values, default values or inherited values.
     */
    fun findApplicationOrDefault(name: String?): Application {
        if (name.isNullOrEmpty()) return default

        val application = apps.firstOrNull { it.name.equals(name, ignoreCase = true) }
        if (application !== null) {
            application.returnTo = if (application.returnTo.isNotEmpty()) application.returnTo else default.returnTo
            application.redirectUri = if (application.redirectUri.isNotEmpty()) application.redirectUri else default.redirectUri
            application.audience = if (application.audience.isNotEmpty()) application.audience else default.audience
            application.scope = if (application.scope.isNotEmpty()) application.scope else default.scope
            application.clientId = if (application.clientId.isNotEmpty()) application.clientId else default.clientId
            application.clientSecret = if (application.clientSecret.isNotEmpty()) application.clientSecret else default.clientSecret
            application.tokenCookieDomain = if (application.tokenCookieDomain.isNotEmpty()) application.tokenCookieDomain else default.tokenCookieDomain
            application.restrictedMethods = if (application.restrictedMethods.isNotEmpty()) application.restrictedMethods else default.restrictedMethods
            application.claims = if (application.claims.isNotEmpty()) application.claims else default.claims
            application.requiredPermissions = if (application.requiredPermissions.isNotEmpty()) application.requiredPermissions else default.requiredPermissions
            return application
        } else return default
    }
}