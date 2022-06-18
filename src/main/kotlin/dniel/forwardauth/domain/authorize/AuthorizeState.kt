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

import java.util.Base64
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
class AuthorizeState private constructor(val originUrl: RequestedUrl, val nonce: AuthorizeNonce) {

    companion object {
        fun create(originUrl: RequestedUrl, nonce: AuthorizeNonce): AuthorizeState {
            return AuthorizeState(originUrl = originUrl, nonce = nonce)
        }

        fun decode(base64: String): AuthorizeState {
            return Json.decodeFromString(
                    serializer(),
                    String(Base64.getDecoder().decode(base64)))
        }
    }

    fun encode(): String {
        return Base64.getEncoder().encodeToString(
                Json.encodeToString(
                        serializer(),
                        this).encodeToByteArray())
    }

    override fun toString(): String {
        return encode()
    }
}