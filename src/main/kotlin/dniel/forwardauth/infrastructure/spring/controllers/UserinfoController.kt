package dniel.forwardauth.infrastructure.spring.controllers

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.infrastructure.auth0.Auth0Client
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*

@RestController
internal class UserinfoController(val properties: AuthProperties, val auth0Client: Auth0Client) {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    /**
     * Sign Out endpoint.
     *
     * @param headers
     * @param response
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/userinfo", method = [RequestMethod.GET], produces = ["application/json"])
    fun signout(@RequestHeader headers: MultiValueMap<String, String>,
                @CookieValue("ACCESS_TOKEN", required = true) accessToken: String): ResponseEntity<String> {
        LOGGER.debug("Get userinfo from Auth0")
        val userinfo = auth0Client.userinfo(accessToken)
        return ResponseEntity.ok(userinfo)
    }
}
