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

import dniel.forwardauth.domain.config.ForwardAuthSettings
import io.micronaut.context.annotation.Value
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.validation.constraints.Min
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

/**
 * # Application settings
 */
@Singleton
open class ForwardAuthSettings(
        @NotNull @Inject override val default: DefaultApplicationSettings,
        @NotEmpty @Inject override val apps: List<ApplicationSettings>) : ForwardAuthSettings {

    @Value("\${domain}")
    @NotEmpty
    override lateinit var domain: String

    @Value("\${token-endpoint}")
    @NotEmpty
    override lateinit var tokenEndpoint: String

    @Value("\${logout-endpoint}")
    @NotEmpty
    override lateinit var logoutEndpoint: String

    @Value("\${userinfo-endpoint}")
    @NotEmpty
    override lateinit var userinfoEndpoint: String

    @Value("\${authorize-url}")
    @NotEmpty
    override lateinit var authorizeUrl: String

    override var nonceMaxAge: Long = 60
}