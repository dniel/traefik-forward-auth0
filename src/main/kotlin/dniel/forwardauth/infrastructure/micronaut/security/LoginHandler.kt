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

import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.handlers.LoginHandler
import jakarta.inject.Singleton

/**
 * Login Handler.
 */
@Singleton
class LoginHandler : LoginHandler{
    override fun loginSuccess(authentication: Authentication?, request: HttpRequest<*>?): MutableHttpResponse<*> {
        TODO("Not yet implemented")
    }

    override fun loginRefresh(authentication: Authentication?, refreshToken: String?, request: HttpRequest<*>?): MutableHttpResponse<*> {
        TODO("Not yet implemented")
    }

    override fun loginFailed(authenticationResponse: AuthenticationResponse?, request: HttpRequest<*>?): MutableHttpResponse<*> {
        TODO("Not yet implemented")
    }

}