package dniel.forwardauth.infrastructure.spring

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.infrastructure.auth0.Auth0Client
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
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
    @RequestMapping("/userinfo", method = [RequestMethod.GET], produces = ["application/json"])
    fun signout(@RequestHeader headers: MultiValueMap<String, String>,
                @CookieValue("ACCESS_TOKEN", required = true) accessToken: String): ResponseEntity<String> {
        if (accessToken.isNullOrEmpty()) {
            LOGGER.info("Access Token not found, no userinfo to look up.")
            return ResponseEntity.badRequest().body("Access Token not found, no userinfo to look up.")
        } else {
            LOGGER.debug("Get userinfo from Auth0")
            val userinfo = auth0Client.userinfo(accessToken)
            return ResponseEntity.ok(userinfo)
        }
    }
}
