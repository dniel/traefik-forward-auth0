package dniel.forwardauth.infrastructure.spring.controllers

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.infrastructure.auth0.Auth0Client
import dniel.forwardauth.infrastructure.siren.Root
import dniel.forwardauth.infrastructure.siren.Siren.APPLICATION_SIREN_JSON
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
    @RequestMapping("/userinfo", method = [RequestMethod.GET], produces = [APPLICATION_SIREN_JSON])
    fun signout(@RequestHeader headers: MultiValueMap<String, String>,
                @CookieValue("ACCESS_TOKEN", required = true) accessToken: String): ResponseEntity<Root> {
        LOGGER.debug("Get userinfo from Auth0")
        val userinfo = auth0Client.userinfo(accessToken)
        val root = Root.newBuilder()
                .title("Userinfo")
                .properties(userinfo)
                .clazz("userinfo")
                .build()

        // TODO: add link
        /*  "links": [
         *       { "rel": [ "self" ], "href": "http://api.x.io/orders/42" },
         *       { "rel": [ "previous" ], "href": "http://api.x.io/orders/41" },
         *       { "rel": [ "next" ], "href": "http://api.x.io/orders/43" }
         *     ]
         */

        return ResponseEntity.ok(root)
    }
}
