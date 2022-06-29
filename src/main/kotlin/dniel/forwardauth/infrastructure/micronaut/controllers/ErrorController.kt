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

import dniel.forwardauth.infrastructure.micronaut.exceptions.PermissionDeniedException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.security.authentication.AuthorizationException
import io.micronaut.views.ViewsRenderer

/**
 * Error handler.
 * This error handler is responsible to display the error page, or error json
 * depending on if it is a browser or api that calls the handler
 */
@Controller("/error")
class ErrorController(private val viewsRenderer: ViewsRenderer<*>) {

    @Error(exception = AuthorizationException::class, global = true)
    fun authorizationDenied(request: HttpRequest<*>): HttpResponse<*> {
        return HttpResponse.ok(viewsRenderer.render("unauthorized", null, request))
                .contentType(MediaType.TEXT_HTML).status(401)
    }

    @Error(exception = PermissionDeniedException::class, global = true)
    fun permissionDenied(request: HttpRequest<*>): HttpResponse<*> {
        return HttpResponse.ok(viewsRenderer.render("unauthorized", null, request))
                .contentType(MediaType.TEXT_HTML).status(401)
    }

    @Error(exception = dniel.forwardauth.infrastructure.micronaut.exceptions.AuthorizationException::class, global = true)
    fun authorizationFailure(request: HttpRequest<*>): HttpResponse<*> {
        return HttpResponse.serverError(viewsRenderer.render("unauthorized", null, request))
                .contentType(MediaType.TEXT_HTML).status(500)
    }

    /**
     * Global catch-all handler.
     */
    @Error(global = true)
    fun default(request: HttpRequest<*>): HttpResponse<*> {
        return HttpResponse.serverError(viewsRenderer.render("default", null, request))
                .contentType(MediaType.TEXT_HTML).status(500)
    }
}