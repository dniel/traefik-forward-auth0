package dniel.forwardauth.infrastructure.spring.filters

import dniel.forwardauth.application.CommandDispatcher
import dniel.forwardauth.application.commandhandlers.AuthenticateHandler
import dniel.forwardauth.domain.shared.Anonymous
import dniel.forwardauth.domain.shared.Authenticated
import dniel.forwardauth.infrastructure.auth0.Auth0Client
import dniel.forwardauth.infrastructure.spring.exceptions.AuthenticationException
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
class AuthenticationTokenFilter(val authenticateHandler: AuthenticateHandler,
                                val commandDispatcher: CommandDispatcher,
                                val auth0Client: Auth0Client) : BaseFilter() {

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

        // to authenticate we need to have some cookies to search for, and also
        // the x-forwarded-host must be set to know which application configuration
        // to use for authentication properties.
        //
        // if already authenticaed, just skip check of cookies because that means
        // that the previous filter using Basic Auth has authenticated the user.
        if (SecurityContextHolder.getContext().authentication.principal is Anonymous) {
            trace("Authenticate using Tokens from Cookies.")
            val accessToken = readCookie(req, "ACCESS_TOKEN")
            val idToken = readCookie(req, "JWT_TOKEN")
            val host = req.getHeader("x-forwarded-host")

            // execute command and get result event.
            val command: AuthenticateHandler.AuthenticateCommand = AuthenticateHandler.AuthenticateCommand(accessToken, idToken, host)
            val event = commandDispatcher.dispatch(authenticateHandler, command) as AuthenticateHandler.AuthenticationEvent
            when (event) {
                is AuthenticateHandler.AuthenticationEvent.Error -> {
                    throw AuthenticationException(event)
                }
                is AuthenticateHandler.AuthenticationEvent.AuthenticatedUser -> {
                    val user = event.user as Authenticated
                    val auth = UsernamePasswordAuthenticationToken(user, "", AuthorityUtils.createAuthorityList(*user.permissions))
                    SecurityContextHolder.getContext().authentication = auth
                }
                is AuthenticateHandler.AuthenticationEvent.AnonymousUser -> {
                    val auth = AnonymousAuthenticationToken(
                            "anonymous",
                            Anonymous,
                            AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"))
                    SecurityContextHolder.getContext().authentication = auth
                }
            }
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

