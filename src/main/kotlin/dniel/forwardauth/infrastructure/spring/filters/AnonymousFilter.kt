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
        if (!hasCookie(req, "ACCESS_TOKEN") || !hasCookie(req, "ID_TOKEN")) {
            MDC.put("userId", "anonymous")

            // just quick skip and continue filters if no cookies present, aka anonymous users.
            SecurityContextHolder.getContext().authentication = AnonymousAuthenticationToken(
                    "anonymous",
                    Anonymous,
                    AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"))

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

