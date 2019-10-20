package dniel.forwardauth.infrastructure.spring.controllers

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.infrastructure.auth0.Auth0Client
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@RestController
internal class SignoutController(val properties: AuthProperties, val auth0Client: Auth0Client) : BaseController() {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    /**
     * Sign Out endpoint.
     *
     * @param headers
     * @param response
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/signout", method = [RequestMethod.GET])
    fun signout(@RequestHeader headers: MultiValueMap<String, String>,
                @RequestHeader("x-forwarded-host") forwardedHost: String?,
                @CookieValue("ACCESS_TOKEN", required = false) accessToken: String?,
                @CookieValue("JWT_TOKEN", required = false) idToken: String?,
                response: HttpServletResponse): ResponseEntity<Unit> {
        LOGGER.debug("Sign out from Auth0")
        val app = properties.findApplicationOrDefault(forwardedHost)
        val signout = auth0Client.signout(app.clientId, app.returnTo)

        LOGGER.debug("Sign out from ForwardAuth (clear cookies)")
        clearCookie(response, "ACCESS_TOKEN", app.tokenCookieDomain)
        clearCookie(response, "JWT_TOKEN", app.tokenCookieDomain)

        if (!signout.isNullOrEmpty()) {
            LOGGER.debug("Signout done, redirect to ${signout}")
            return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).header("location", signout).build()
        } else {
            LOGGER.debug("Signout done.")
            return ResponseEntity.noContent().build()
        }
    }
}
