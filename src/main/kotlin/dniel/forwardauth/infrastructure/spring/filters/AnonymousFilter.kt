package dniel.forwardauth.infrastructure.spring.filters

import dniel.forwardauth.application.AuthenticateHandler
import dniel.forwardauth.application.CommandDispatcher
import dniel.forwardauth.domain.shared.Anonymous
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.AnonymousAuthenticationToken
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
class AnonymousFilter(val authenticateHandler: AuthenticateHandler,
                      val commandDispatcher: CommandDispatcher) : BaseFilter() {

    /**
     * Filter
     *
     */
    override fun doFilterInternal(req: HttpServletRequest, resp: HttpServletResponse, chain: FilterChain) {
        trace("AnonymousFilter start")
        if (SecurityContextHolder.getContext().authentication == null || req.cookies.isNullOrEmpty()) {
            MDC.put("userId", "anonymous")

            // just quick skip and continue filters if no cookies present, aka anonymous users.
            SecurityContextHolder.getContext().authentication = AnonymousAuthenticationToken(
                    "anonymous",
                    Anonymous,
                    AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"))

            trace("Anonymous authentication set.")
        }

        chain.doFilter(req, resp)
        trace("AnonymousFilter filter done")
    }

    fun readCookie(req: HttpServletRequest, key: String): String? {
        return req.cookies.filter { c -> key.equals(c.getName()) }.map { cookie -> cookie.value }.firstOrNull()
    }
}

