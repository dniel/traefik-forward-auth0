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

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.Base64

class AuthorizeState private constructor(val originUrl: RequestedUrl, val nonce: AuthorizeNonce) {

    companion object {
        val JSON = jacksonObjectMapper()
        fun create(originUrl: RequestedUrl, nonce: AuthorizeNonce): AuthorizeState {
            return AuthorizeState(originUrl = originUrl, nonce = nonce)
        }

        fun decode(base64: String): AuthorizeState {
            return JSON.readValue(Base64.getDecoder().decode(base64), AuthorizeState::class.java)
        }
    }

    fun toJson(): String {
        return JSON.writeValueAsString(this)
    }

    fun encode(): String {
        return Base64.getEncoder().encodeToString(JSON.writeValueAsBytes(this))
    }

    override fun toString(): String {
        return encode()
    }
}