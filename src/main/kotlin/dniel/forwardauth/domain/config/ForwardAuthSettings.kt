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

package dniel.forwardauth.domain.config

import dniel.forwardauth.domain.config.service.FindApplicationOrDefault

/**
 *
 */
interface ForwardAuthSettings {
    val domain: String
    val tokenEndpoint: String
    val logoutEndpoint: String
    val userinfoEndpoint: String
    val authorizeUrl: String
    val nonceMaxAge: Long

    /**
     *
     */
    val default: ApplicationSettings

    /**
     *
     */
    val apps: List<ApplicationSettings>

    /**
     * Return application with application specific values, default values or inherited values.
     */
    fun findApplicationOrDefault(name: String?) =
            FindApplicationOrDefault.findApplicationOrDefault(name,apps,default)
}