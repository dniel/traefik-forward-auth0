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

package dniel.forwardauth.domain

/**
 * A generic user interface with access token and id token
 */
interface User {
    val accessToken: Token
    val idToken: Token
}

/**
 * Authenticated user object
 */
open class Authenticated(override val accessToken: JwtToken,
                         override val idToken: Token,
                         val userinfo: Map<String, String>) : User {
    val sub: String = accessToken.subject()
    val permissions: Array<String> = accessToken.permissions()
    override fun toString(): String = accessToken.subject()
}

/**
 * Anonymous user object.
 */
object Anonymous : User {
    override val accessToken: Token
        get() = InvalidToken("anonymous")
    override val idToken: Token
        get() = InvalidToken("anonymous")

    override fun toString(): String {
        return "anonymous"
    }
}