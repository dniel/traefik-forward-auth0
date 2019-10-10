package dniel.forwardauth.infrastructure.spring.filters

import com.google.common.annotations.VisibleForTesting
import org.slf4j.MDC
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import java.io.IOException

/**
 * Attach a user identifier to requests / responses / logs for auditing
 * User details can be found in the Auth0 Dashboard: https://manage.auth0.com/#/users -> Search the "user_id" by Lucene Syntax
 */
@Component
class UserIdFilter : GenericFilterBean() {

    internal val authentication: Authentication?
        @VisibleForTesting
        get() = SecurityContextHolder.getContext().authentication

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        if (authentication != null) {
            MDC.put(MDC_KEY, authentication!!.name)
        }
        try {
            chain.doFilter(request, response)
        } finally {
            MDC.remove(MDC_KEY)
        }
    }

    @VisibleForTesting
    internal fun get(): String {
        return MDC.get(MDC_KEY)
    }

    companion object {
        private val MDC_KEY = "userId"
    }
}