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

package dniel.forwardauth.domain.config.service

import dniel.forwardauth.domain.config.ApplicationSettings
import dniel.forwardauth.domain.stereotypes.DomainService

/**
 * # Find Application Or Default Settings.
 */
class FindApplicationOrDefault : DomainService{

    companion object{
        /**
         * ## Return application with application specific values, default values or inherited values.
         */
        fun findApplicationOrDefault(name: String?,
                                     apps : List<ApplicationSettings>,
                                     default : ApplicationSettings): ApplicationSettings {
            if (name.isNullOrEmpty()) return default

            val application = apps.firstOrNull { it.name.equals(name, ignoreCase = true) }
            return if (application !== null) {
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
                application
            } else default
        }
    }
}