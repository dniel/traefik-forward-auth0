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

/**
 *
 */
interface ApplicationSettings {
    var name: String
    var clientId: String
    var clientSecret: String
    var audience: String
    var scope: String
    var redirectUri: String
    var tokenCookieDomain: String
    var restrictedMethods: Array<String>
    var requiredPermissions: Array<String>
    var claims: Array<String>
    var returnTo: String
}