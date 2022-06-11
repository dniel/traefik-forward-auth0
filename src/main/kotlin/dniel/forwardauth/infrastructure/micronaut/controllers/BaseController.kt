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

package dniel.forwardauth.infrastructure.micronaut.controllers

import io.micronaut.core.type.Headers
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.cookie.Cookie
import org.slf4j.LoggerFactory
import java.util.stream.Collectors
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Just a base class to provide some common functiosn for rest controllers.
 */
abstract class BaseController {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    fun addCookie(response: MutableHttpResponse<*>, name: String, value: String, domain: String, maxAge: Long) {
        val nonceCookie = Cookie.of(name, value)
            .domain(domain)
            .maxAge(maxAge)
            .path("/")
        response.cookie(nonceCookie)
    }

    fun clearCookie(response: MutableHttpResponse<*>, name: String, domain: String) {
        val nonceCookie = Cookie.of(name, "deleted")
            .domain(domain)
            .maxAge(0)
            .path("/")
        response.cookie(nonceCookie)
    }

    fun trace(message: String) {
        logger.trace { message }
    }

    fun error(message: String) {
        logger.error { message }
    }

    fun info(message: String){
        logger.info { message }
    }

    fun debug(message: String){
        logger.info { message }
    }

    fun printHeaders(headers: Headers) {
        if (LOGGER.isTraceEnabled) {
            headers.forEach { (key, value) -> trace(String.format("Header '%s' = %s", key, value.stream().collect(Collectors.joining("|")))) }
        }
    }
}