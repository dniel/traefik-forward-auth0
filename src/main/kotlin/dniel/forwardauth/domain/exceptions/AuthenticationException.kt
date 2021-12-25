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

import dniel.forwardauth.application.commandhandlers.AuthenticateHandler

/**
 * Generic Authentication Error.
 * Should stop execution and not give access to be sure that we dont give access to someone that shouldnt be allowed.
 */
open class AuthenticationException : ApplicationException {
    constructor(error: AuthenticateHandler.AuthenticationEvent.Error) : super(error.reason)
}