package dniel.forwardauth.infrastructure.spring.controllers

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.domain.shared.Anonymous
import dniel.forwardauth.domain.shared.Authenticated
import dniel.forwardauth.domain.shared.User
import dniel.forwardauth.infrastructure.siren.Action
import dniel.forwardauth.infrastructure.siren.Link
import dniel.forwardauth.infrastructure.siren.Root
import dniel.forwardauth.infrastructure.siren.Siren
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController()
internal class RootController(val properties: AuthProperties) {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    /**
     * Sign Out endpoint.
     *
     * @param headers
     * @param response
     * @param page is the offset from where to return events
     * @param size is the number of events to return per page
     */
    @RequestMapping("/",
            method = [RequestMethod.GET],
            produces = [Siren.APPLICATION_SIREN_JSON])
    fun root(): ResponseEntity<Root> {
        LOGGER.debug("Get root context")
        val user = SecurityContextHolder.getContext().authentication.principal as User
        val authorities = SecurityContextHolder.getContext().authentication.authorities

        val links = mutableListOf<Link>()
        val actions = mutableListOf<Action>()

        // when user is already logged in and authenticated, show
        // links available for authenticated users.
        when (user) {
            is Authenticated -> {
                // add action to signout
                actions += Action(name = "signout", method = "GET", href = URI("/signout"), title = "Signout")

                // add link to userinfo
                links += Link(
                        type = Siren.APPLICATION_SIREN_JSON,
                        clazz = listOf("userinfo"),
                        title = "Userinfo for current user",
                        rel = listOf("userinfo"),
                        href = URI("/userinfo"))

                // add link to retrieve application events.
                if (isAdministrator(authorities)) {
                    links += Link(
                            type = Siren.APPLICATION_SIREN_JSON,
                            clazz = listOf("event", "collection"),
                            title = "Application events",
                            rel = listOf("events"),
                            href = URI("/events"))
                }
            }

            is Anonymous -> {
                // add action to signout
                actions += Action(name = "signin", method = "GET", href = URI("/signin"), title = "Sign in")
            }
        }

        val root = Root.newBuilder()
                .title("ForwardAuth")
                .links(links)
                .actions(actions)
                .build()

        return ResponseEntity.ok(root)
    }

    private fun isAdministrator(authorities: MutableCollection<out GrantedAuthority>) =
            authorities.find { it.authority == "admin:forwardauth" } != null


}
