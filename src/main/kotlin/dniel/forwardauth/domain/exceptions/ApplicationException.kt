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

package dniel.forwardauth.domain.exceptions

import io.micronaut.http.HttpStatus
import io.micronaut.http.HttpStatus.INTERNAL_SERVER_ERROR

/**
 * Generic error, but should stop execution and not give access to be sure
 * that we don't give access to someone that shouldn't be allowed.
 */
open class ApplicationException : io.micronaut.http.exceptions.HttpStatusException {
    constructor() : super(
        INTERNAL_SERVER_ERROR,
        "Unknown state, dont know what to do, better block access."
    )

    constructor(message: String) : super(INTERNAL_SERVER_ERROR, message)
    constructor(message: String, status: HttpStatus) : super(status, message)
}