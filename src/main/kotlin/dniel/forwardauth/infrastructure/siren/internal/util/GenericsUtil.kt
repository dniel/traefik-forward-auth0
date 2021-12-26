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

package dniel.forwardauth.infrastructure.siren.internal.util

internal fun Any?.asList(): List<Any?> {
    require(this is List<*>) { "Casting to List failed. Found type ${this?.javaClass}" }
    return this
}

internal fun Any?.asNonNullStringList(): List<String> {
    require(this is List<*>) { "Casting to List failed. Found type ${this?.javaClass}" }
    this.forEach { item ->
        require(item is String) { "Casting to List of Strings. Found item ${item?.javaClass}" }
    }

    @Suppress("UNCHECKED_CAST")
    return this as List<String>
}

internal fun Any?.asMap(): Map<String, Any?> {
    require(this is Map<*, *>) { "Casting to Map failed. Found type ${this?.javaClass}" }

    @Suppress("UNCHECKED_CAST")
    return this as Map<String, Any>
}