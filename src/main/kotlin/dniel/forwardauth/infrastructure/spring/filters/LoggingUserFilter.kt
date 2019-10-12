package dniel.forwardauth.infrastructure.spring.filters

import org.slf4j.MDC
import org.springframework.core.annotation.Order
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Attach a user identifier to requests / responses / logs for auditing
 * User details can be found in the Auth0 Dashboard: https://manage.auth0.com/#/users -> Search the "user_id" by Lucene Syntax
 */
@Component
class LoggingUserFilter : BaseFilter() {

    override fun doFilterInternal(req: HttpServletRequest, resp: HttpServletResponse, chain: FilterChain) {
        trace("LoggingUserFilter start")
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication != null) {
            MDC.put(MDC_KEY, authentication.name)
        }
        try {
            chain.doFilter(req, resp)
        } finally {
            MDC.remove(MDC_KEY)
        }
        trace("LoggingUserFilter filter done")
    }

    companion object {
        private val MDC_KEY = "userId"
    }
}