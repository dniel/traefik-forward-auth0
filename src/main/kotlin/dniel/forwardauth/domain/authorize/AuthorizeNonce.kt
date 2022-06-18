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

import java.util.UUID
import kotlinx.serialization.Serializable

/**
 * The nonce parameter value needs to include per-session authorizeState and be unguessable to attackers.
 *
 * One method to achieve this for Web Server Clients is to store a cryptographically random value
 * as an HttpOnly session cookie and use a cryptographic hash of the value as the nonce parameter.
 * In that case, the nonce in the returned ID Token is compared to the hash of the session cookie to
 * detect ID Token replay by third parties. A related method applicable to JavaScript Clients is to
 * store the cryptographically random value in HTML5 local storage and use a cryptographic hash of this value.
 *
 * https://openid.net/specs/openid-connect-core-1_0.html#NonceNotes
 * https://tools.ietf.org/html/draft-ietf-oauth-v2-threatmodel-06#section-4.6.2
 * https://tools.ietf.org/html/draft-ietf-oauth-v2-threatmodel-06#section-3.6
 */

@Serializable
class AuthorizeNonce(val value: String) {

    companion object {
        fun generate(): AuthorizeNonce {
            return AuthorizeNonce(UUID.randomUUID().toString().replace("-", ""))
        }
    }

    override fun toString(): String = value
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AuthorizeNonce

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}