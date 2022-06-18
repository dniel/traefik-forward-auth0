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

import java.net.URI
import kotlinx.serialization.Serializable

@Serializable
class RequestedUrl(val protocol: String, val host: String, val uri: String, val method: String) {

    override fun toString(): String {
        return "$protocol://$host$uri".toLowerCase()
    }

    fun startsWith(url: String): Boolean = this.toString().startsWith(url, ignoreCase = true)

    fun uri(): URI = URI.create(this.toString())
}