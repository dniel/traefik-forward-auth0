package dniel.forwardauth.infrastructure.spring.filters

import dniel.forwardauth.application.AuthenticateHandler
import dniel.forwardauth.application.CommandDispatcher
import dniel.forwardauth.domain.shared.Anonymous
import org.slf4j.MDC
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * Anonymous user filter.
 * To be sure that we always have a user object in authentication context
 * this filter will check if user credentials, tokens, is set in the cookie headers.
 * If the cookie headers or token cookies is missing the authentication is set
 * to be an anonymous user.
 */
@Component
class AnonymousFilter(val authenticateHandler: AuthenticateHandler,
                      val commandDispatcher: CommandDispatcher) : BaseFilter() {

    /**
     * Anonymous user filter.
     * dummy change
     *
     */
    override fun doFilterInternal(req: HttpServletRequest, resp: HttpServletResponse, chain: FilterChain) {
        trace("AnonymousFilter start")
        if (!hasCookie(req, "ACCESS_TOKEN") || !hasCookie(req, "ID_TOKEN")) {
            SecurityContextHolder.getContext().authentication = AnonymousAuthenticationToken(
                    "anonymous",
                    Anonymous,
                    AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"))
            MDC.put("userId", SecurityContextHolder.getContext().authentication.name)

            trace("Either ACCESS_TOKEN or ID_TOKEN was missing, Anonymous authentication set.")
        }

        chain.doFilter(req, resp)
        trace("AnonymousFilter filter done")
    }

    fun hasCookie(req: HttpServletRequest, key: String): Boolean {
        return if (req.cookies.isNullOrEmpty()) false
        else req.cookies.filter { c -> key.equals(c.getName()) }.firstOrNull() != null
    }
}

