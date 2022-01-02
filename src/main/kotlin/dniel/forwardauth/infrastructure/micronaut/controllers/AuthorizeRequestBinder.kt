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

import io.micronaut.core.bind.ArgumentBinder.BindingResult
import io.micronaut.core.convert.ArgumentConversionContext
import io.micronaut.core.type.Argument
import io.micronaut.core.type.Headers
import io.micronaut.http.HttpRequest
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder
import jakarta.inject.Singleton
import java.util.Optional
import java.util.stream.Collectors
import org.slf4j.LoggerFactory

@Singleton
class AuthorizeRequestBinder : TypedRequestArgumentBinder<AuthorizeRequest> {

    companion object {
        val TYPE: Argument<AuthorizeRequest> = Argument.of(AuthorizeRequest::class.java)
        val LOGGER = LoggerFactory.getLogger(AuthorizeRequestBinder::class.java)
    }

    override fun bind(context: ArgumentConversionContext<AuthorizeRequest>, source: HttpRequest<*>): BindingResult<AuthorizeRequest> {
        LOGGER.trace("Bind AuthorizeRequest")
        val httpHeaders = source.headers
        printHeaders(httpHeaders)

        val host = httpHeaders.get("x-forwarded-host")
        val proto = httpHeaders.get("x-forwarded-proto")
        val uri = httpHeaders.get("x-forwarded-uri")
        val method = httpHeaders.get("x-forwarded-method")

        // need all of these to authorize request, throw validation exception if missing.
        if(host== null || proto== null || uri== null || method == null)
            throw IllegalArgumentException("Missing required parameter host, proto, uri or method")

        return BindingResult { Optional.of(AuthorizeRequest(host, proto, uri, method)) }
    }

    override fun argumentType(): Argument<AuthorizeRequest> {
        return TYPE
    }

    private fun printHeaders(headers: Headers) {
        if (LOGGER.isTraceEnabled) {
            headers.forEach { (key, value) -> LOGGER.trace(String.format("Header '%s' = %s", key, value.stream().collect(Collectors.joining("|")))) }
        }
    }
}