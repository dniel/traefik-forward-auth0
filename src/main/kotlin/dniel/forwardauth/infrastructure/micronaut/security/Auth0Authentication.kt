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

package dniel.forwardauth.infrastructure.micronaut.security

import dniel.forwardauth.domain.User
import io.micronaut.security.authentication.ServerAuthentication

class Auth0Authentication private constructor(
        val user: User,
        name: String,
        roles: List<String>,
        attributes: Map<String, Any>) : ServerAuthentication(name, roles, attributes) {

    companion object {
        fun fromUser(user: User) = Auth0Authentication(user, user.id, user.permissions.toList(), user.userinfo)
    }
}