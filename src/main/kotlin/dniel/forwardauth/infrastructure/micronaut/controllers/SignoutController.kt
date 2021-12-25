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

import dniel.forwardauth.application.CommandDispatcher
import dniel.forwardauth.application.commandhandlers.SignoutHandler
import io.micronaut.http.annotation.Controller
import org.slf4j.LoggerFactory

/**
 * Sign out controller.
 */
@Controller
internal class SignoutController(val signoutHandler: SignoutHandler,
                                 val commandDispatcher: CommandDispatcher) : BaseController() {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

//    /**
//     * Sign Out endpoint.
//     *
//     * @param headers
//     * @param response
//     */
//    @Get("/signout")
//    @Secured(SecurityRule.IS_AUTHENTICATED)
//    fun signout(headers: HttpHeaders,
//                @Header("x-forwarded-host") forwardedHost: String,
//                @CookieValue("ACCESS_TOKEN", required = false) accessToken: String): MutableHttpResponse<Any> {
//        val command: SignoutHandler.SignoutCommand = SignoutHandler.SignoutCommand(forwardedHost, accessToken)
//        val signoutEvent = commandDispatcher.dispatch(signoutHandler, command) as SignoutHandler.SignoutEvent
//
//        return when (signoutEvent) {
//            is SignoutHandler.SignoutEvent.SignoutComplete -> {
//                clearSessionCookies(signoutEvent.app, response)
//                ResponseEntity.noContent().build()
//            }
//            is SignoutHandler.SignoutEvent.SignoutRedirect -> {
//                val response: MutableHttpResponse<Any> = HttpResponse.temporaryRedirect(signinEvent.redirectTo)
//                clearSessionCookies(signoutEvent.app, response)
//                ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).header("location", signoutEvent.redirectUrl).build()
//            }
//            is SignoutHandler.SignoutEvent.Error -> throw ApplicationException(signoutEvent.reason)
//        }
//    }
//
//    /**
//     * Remove session cookies from browser.
//     */
//    private fun clearSessionCookies(app: Application, response: HttpServletResponse) {
//        LOGGER.debug("Clear session cookues, access token and id token.")
//        clearCookie(response, "ACCESS_TOKEN", app.tokenCookieDomain)
//        clearCookie(response, "JWT_TOKEN", app.tokenCookieDomain)
//    }
}
