package dniel.forwardauth.infrastructure.spring.filters

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.application.CommandDispatcher
import dniel.forwardauth.application.commandhandlers.AuthenticateHandler
import dniel.forwardauth.domain.shared.Anonymous
import dniel.forwardauth.infrastructure.auth0.Auth0Client
import org.slf4j.MDC
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*
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
class AuthenticationBasicFilter(properties: AuthProperties,
                                authenticateHandler: AuthenticateHandler,
                                commandDispatcher: CommandDispatcher,
                                val auth0Client: Auth0Client) : BaseFilter(properties, authenticateHandler, commandDispatcher) {


    /**
     * Perform filtering.
     *
     */
    override fun doFilterInternal(req: HttpServletRequest, resp: HttpServletResponse, chain: FilterChain) {
        trace("AuthenticationBasicFilter start")
        if (req.getHeader("x-forwarded-host") == null) {
            trace("Missing x-forwarded-host header to authenticate, skip basic authentication..")
            return chain.doFilter(req, resp)
        }

        // use basic auth to retrieve access token from Auth0 using Client Credentials.
        if (SecurityContextHolder.getContext().authentication.principal is Anonymous) {
            val authorization: String? = req.getHeader("Authorization")
            if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
                trace("Found Basic authentication header, parse username and password")
                val base64Credentials = authorization.substring("Basic".length).trim { it <= ' ' }
                val credDecoded: ByteArray = Base64.getDecoder().decode(base64Credentials)
                val credentials = String(credDecoded, StandardCharsets.UTF_8)
                val (username, password) = credentials.split(":".toRegex(), 2).toTypedArray()
                val host = req.getHeader("x-forwarded-host")

                // Request access_token by client credentials from Auth0.
                val audience = properties.findApplicationOrDefault(host).audience
                val cachedToken = auth0Client.clientCredentialsExchange(username, password, audience)
                val accessToken = cachedToken.getString("access_token")

                // authorize user, put resulting user in SecurityContextHolder
                // when using client_credentials you dont have any id token
                // so just send null as token.
                authorize(accessToken, null, host)
                MDC.put("userId", SecurityContextHolder.getContext().authentication.name)
            } else {
                trace("No authentication headers found to basic auth.")
            }
        }
        chain.doFilter(req, resp)
        trace("AuthenticationBasicFilter filter done")
    }

}

