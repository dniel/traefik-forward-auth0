package dniel.forwardauth.infrastructure.spring

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.infrastructure.auth0.Auth0Client
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

@RestController
internal class SignoutController(val properties: AuthProperties, val auth0Client: Auth0Client) {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    /**
     * Sign Out endpoint.
     *
     * @param headers
     * @param response
     */
    @RequestMapping("/signout", method = [RequestMethod.GET])
    fun signout(@RequestHeader headers: MultiValueMap<String, String>,
                @RequestHeader("x-forwarded-host") forwardedHost: String?,
                @CookieValue("ACCESS_TOKEN", required = false) accessToken: String?,
                @CookieValue("JWT_TOKEN", required = false) idToken: String?,
                response: HttpServletResponse): ResponseEntity<Unit> {
        if (accessToken.isNullOrEmpty()) {
            LOGGER.info("Access Token not found, no user to sign out.")
            return ResponseEntity.badRequest().build<Unit>()
        } else {
            val app = properties.findApplicationOrDefault(forwardedHost)
            val accessTokenCookie = Cookie("ACCESS_TOKEN", "delete")
            val jwtCookie = Cookie("JWT_TOKEN", "delete")

            LOGGER.debug("Sign out from Auth0")
            val signout = auth0Client.signout(app.clientId, app.returnTo)

            LOGGER.debug("Sign out from ForwardAuth")
            // clear cookies.
            accessTokenCookie.domain = app.tokenCookieDomain
            accessTokenCookie.maxAge = 0
            accessTokenCookie.path = "/"
            accessTokenCookie.isHttpOnly = true
            jwtCookie.domain = app.tokenCookieDomain
            jwtCookie.maxAge = 0
            jwtCookie.path = "/"
            jwtCookie.isHttpOnly = true

            response.addCookie(accessTokenCookie)
            response.addCookie(jwtCookie)
            if (!signout.isNullOrEmpty()) {
                LOGGER.debug("Signout done, redirect to ${signout}")
                return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).header("location", signout).build()
            } else {
                LOGGER.debug("Signout done.")
                return ResponseEntity.noContent().build()
            }
        }
    }
}
