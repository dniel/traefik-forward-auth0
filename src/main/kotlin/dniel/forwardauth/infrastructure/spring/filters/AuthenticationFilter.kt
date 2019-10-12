package dniel.forwardauth.infrastructure.spring.filters

import dniel.forwardauth.application.AuthenticateHandler
import dniel.forwardauth.application.CommandDispatcher
import org.slf4j.MDC
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * Validate Tokens and if valid, set as authenticated user.
 */
@Component
class AuthenticationFilter(val authenticateHandler: AuthenticateHandler,
                           val commandDispatcher: CommandDispatcher) : BaseFilter() {

    /**
     * Filter
     *
     */
    override fun doFilterInternal(req: HttpServletRequest, resp: HttpServletResponse, chain: FilterChain) {
        trace("AuthenticationFilter start")
        if (req.cookies.isNullOrEmpty()) {
            trace("No cookies found, skip token validation.. anonymous session.")
        } else {
            trace("Found cookies, validate authentication tokens.")
            val accessToken = readCookie(req, "ACCESS_TOKEN")
            val idToken = readCookie(req, "JWT_TOKEN")
            val host = req.getHeader("x-forwarded-host")

            // execute command and get result event.
            val command: AuthenticateHandler.AuthenticateCommand = AuthenticateHandler.AuthenticateCommand(accessToken, idToken, host)
            val event = commandDispatcher.dispatch(authenticateHandler, command) as AuthenticateHandler.AuthentiationEvent

            try {
                when {
                    // When Authenticated
                    event is AuthenticateHandler.AuthentiationEvent.AuthenticatedEvent -> {
                        val user = event.user
                        val auth = UsernamePasswordAuthenticationToken(user, user.password, user.authorities)
                        SecurityContextHolder.getContext().authentication = auth
                    }

                    event is AuthenticateHandler.AuthentiationEvent.AnonymousUserEvent -> {
                        SecurityContextHolder.getContext().authentication = AnonymousAuthenticationToken(
                                "anonymous",
                                "anonymous",
                                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"))
                    }

                }
                MDC.put("userId", SecurityContextHolder.getContext().authentication.name)
            } catch (e: Exception) {
                // clear context if something crashes to avoid partly initialized user session for next requests.
                SecurityContextHolder.clearContext()
            }
        }
        chain.doFilter(req, resp)
        trace("AuthenticationFilter filter done")
    }

    fun readCookie(req: HttpServletRequest, key: String): String? {
        return req.cookies.filter { c -> key.equals(c.getName()) }.map { cookie -> cookie.value }.firstOrNull()
    }
}

