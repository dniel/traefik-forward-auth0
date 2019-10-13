package dniel.forwardauth.infrastructure.spring.filters

import dniel.forwardauth.application.AuthenticateHandler
import dniel.forwardauth.application.CommandDispatcher
import dniel.forwardauth.domain.shared.Anonymous
import dniel.forwardauth.domain.shared.Authenticated
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
 * The Authentication filter will use the parameters in
 * <li/>ACCESS_TOKEN cookie
 * <li/>ID_TOKEN cookie
 * <li/>x-forwarded-host
 * <p/>
 *
 * When all of them are found, it will parse and validate the content to
 * authenticate the request.
 * <p/>
 *
 * The main classes responsible for authenticate the user
 * @see dniel.forwardauth.application.AuthenticateHandler
 * @see dniel.forwardauth.domain.authorize.service.Authenticator
 * @see dniel.forwardauth.domain.authorize.service.AuthenticatorStateMachine
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

        // to authenticate we need to have some cookies to search for, and also
        // the x-forwarded-host must be set to know which application configuration
        // to use for authentication properties.
        if (req.cookies != null && req.getHeader("x-forwarded-host") != null) {
            trace("Found cookies, validate authentication tokens.")
            val accessToken = readCookie(req, "ACCESS_TOKEN")
            val idToken = readCookie(req, "JWT_TOKEN")
            val host = req.getHeader("x-forwarded-host")

            // execute command and get result event.
            val command: AuthenticateHandler.AuthenticateCommand = AuthenticateHandler.AuthenticateCommand(accessToken, idToken, host)
            val event = commandDispatcher.dispatch(authenticateHandler, command) as AuthenticateHandler.AuthentiationEvent

            try {
                // could be a Anonymous user or Authenticated User.
                val user = event.user
                when {
                    user is Authenticated -> {
                        val auth = UsernamePasswordAuthenticationToken(user, "", user.authorities)
                        SecurityContextHolder.getContext().authentication = auth
                    }

                    user is Anonymous -> {
                        val auth = AnonymousAuthenticationToken(
                                "anonymous",
                                user,
                                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"))
                        SecurityContextHolder.getContext().authentication = auth
                    }

                }
                MDC.put("userId", SecurityContextHolder.getContext().authentication.name)
            } catch (e: Exception) {
                // clear context if something crashes to avoid partly initialized user session for next requests.
                SecurityContextHolder.clearContext()
            }

        } else {
            trace("Missing cookies or x-forwarded-host header to authenticate,  skip token validation.. anonymous session.")
        }
        chain.doFilter(req, resp)
        trace("AuthenticationFilter filter done")
    }

    fun readCookie(req: HttpServletRequest, key: String): String? {
        return req.cookies.filter { c -> key.equals(c.getName()) }.map { cookie -> cookie.value }.firstOrNull()
    }
}

