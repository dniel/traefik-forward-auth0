package dniel.forwardauth.infrastructure.spring.filters

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.application.CommandDispatcher
import dniel.forwardauth.application.commandhandlers.AuthenticateHandler
import dniel.forwardauth.domain.shared.Anonymous
import dniel.forwardauth.infrastructure.auth0.Auth0Client
import org.slf4j.MDC
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
class AuthenticationTokenFilter(properties: AuthProperties,
                                authenticateHandler: AuthenticateHandler,
                                commandDispatcher: CommandDispatcher) : BaseFilter(properties, authenticateHandler, commandDispatcher) {

    /**
     * Perform filtering.
     *
     */
    override fun doFilterInternal(req: HttpServletRequest, resp: HttpServletResponse, chain: FilterChain) {
        trace("AuthenticationFilter start")

        if (req.getHeader("x-forwarded-host") == null) {
            trace("Missing x-forwarded-host header to authenticate, skip cookie authentication..")
            return chain.doFilter(req, resp)
        }

        // if already authenticated, just skip check of cookies because that means
        // that the previous filter using Basic Auth has authenticated the user.
        if (SecurityContextHolder.getContext().authentication.principal is Anonymous) {
            trace("Authenticate using Tokens from Cookies.")
            val accessToken = readCookie(req, "ACCESS_TOKEN")
            val idToken = readCookie(req, "JWT_TOKEN")
            val host = req.getHeader("x-forwarded-host")

            // authorize user, put resulting user in SecurityContextHolder
            authorize(accessToken, idToken, host)
            MDC.put("userId", SecurityContextHolder.getContext().authentication.name)
        } else {
            trace("Already authenticated, skip cookie authentication.")
        }
        chain.doFilter(req, resp)
        trace("AuthenticationFilter filter done")
    }

    fun readCookie(req: HttpServletRequest, key: String): String? {
        if (req.cookies == null) return null
        return req.cookies.filter { c -> key.equals(c.getName()) }.map { cookie -> cookie.value }.firstOrNull()
    }
}

