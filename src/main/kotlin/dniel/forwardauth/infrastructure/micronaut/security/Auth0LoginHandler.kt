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
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.cookie.Cookie
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.handlers.RedirectingLoginHandler
import jakarta.inject.Singleton
import java.net.URI
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * # Login Handler.
 *
 * TODO
 * - should handle what to do after a successful login
 * - should handle what to do after a failed login
 *
 */
@Singleton
class Auth0LoginHandler : RedirectingLoginHandler {
    override fun loginSuccess(authentication: Authentication, request: HttpRequest<*>): MutableHttpResponse<*> {
        val redirectTo = authentication.attributes["REDIRECT_TO"] as URI
        logger.info { "SignInSuccessful, redirect to '$redirectTo'" }
        val response: MutableHttpResponse<Any> = HttpResponse.temporaryRedirect(redirectTo)
        addCookie(response, "ACCESS_TOKEN",
                authentication.attributes["ACCESS_TOKEN"] as String,
                authentication.attributes["COOKIE_DOMAIN"] as String,
                authentication.attributes["EXPIRES_IN"] as Long)
        addCookie(response, "ID_TOKEN",
                authentication.attributes["ID_TOKEN"] as String,
                authentication.attributes["COOKIE_DOMAIN"] as String,
                authentication.attributes["EXPIRES_IN"] as Long)
        clearCookie(response, "AUTH_NONCE", authentication.attributes["COOKIE_DOMAIN"] as String)
        return response
    }

    override fun loginRefresh(authentication: Authentication, refreshToken: String, request: HttpRequest<*>): MutableHttpResponse<*> {
        TODO("Not implemented yet.")
    }

    override fun loginFailed(authenticationResponse: AuthenticationResponse, request: HttpRequest<*>): MutableHttpResponse<*> {
        return HttpResponse.status<String>(HttpStatus.UNAUTHORIZED).body("failed")
    }

    fun addCookie(response: MutableHttpResponse<Any>, name: String, value: String, domain: String, maxAge: Long) {
        val nonceCookie = Cookie.of(name, value)
                .domain(domain)
                .maxAge(maxAge)
                .path("/")
        response.cookie(nonceCookie)
    }

    fun clearCookie(response: MutableHttpResponse<Any>, name: String, domain: String) {
        val nonceCookie = Cookie.of(name, "deleted")
                .domain(domain)
                .maxAge(0)
                .path("/")
        response.cookie(nonceCookie)
    }

}