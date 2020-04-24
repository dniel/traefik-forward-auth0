package dniel.forwardauth.infrastructure.spring.filters

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.application.CommandDispatcher
import dniel.forwardauth.application.commandhandlers.AuthenticateHandler
import dniel.forwardauth.domain.shared.Anonymous
import dniel.forwardauth.domain.shared.Authenticated
import dniel.forwardauth.infrastructure.auth0.Auth0Client
import dniel.forwardauth.infrastructure.spring.exceptions.AuthenticationException
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Base filter class with common filter features.
 */
abstract class BaseFilter(val properties: AuthProperties,
                          val authenticateHandler: AuthenticateHandler,
                          val commandDispatcher: CommandDispatcher) : OncePerRequestFilter() {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    /**
     * Authorize user.
     * Set the current user in the SecurityContextHolder or
     * set Anonymous user if no valid user found.
     */
    fun authorize(accessToken: String?, idToken: String?, host: String) {
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
    }

    fun trace(message: String) {
        LOGGER.trace(message)
    }

    fun error(message: String) {
        LOGGER.error(message)
    }

}