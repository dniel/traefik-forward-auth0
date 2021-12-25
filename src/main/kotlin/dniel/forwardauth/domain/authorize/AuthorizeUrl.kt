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

package dniel.forwardauth.domain.authorize

import dniel.forwardauth.domain.Application
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class AuthorizeUrl(val authorizeUrl: String,
                   val app: Application,
                   val authorizeState: AuthorizeState) {

    override fun toString(): String {
        val scopes = URLEncoder.encode(app.scope, StandardCharsets.UTF_8.toString());
        val audience = URLEncoder.encode(app.audience, StandardCharsets.UTF_8.toString());
        val redirectUri = URLEncoder.encode(app.redirectUri, StandardCharsets.UTF_8.toString());
        return "$authorizeUrl?audience=${audience}&scope=${scopes}&response_type=code&client_id=${app.clientId}&redirect_uri=${redirectUri}&state=$authorizeState"
    }

    fun toURI(): URI = URI.create(toString())
}