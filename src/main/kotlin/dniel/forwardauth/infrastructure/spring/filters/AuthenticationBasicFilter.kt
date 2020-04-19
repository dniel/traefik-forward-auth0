package dniel.forwardauth.infrastructure.spring.filters

import dniel.forwardauth.application.CommandDispatcher
import dniel.forwardauth.application.commandhandlers.AuthenticateHandler
import dniel.forwardauth.infrastructure.auth0.Auth0Client
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
class AuthenticationBasicFilter(val authenticateHandler: AuthenticateHandler,
                                val commandDispatcher: CommandDispatcher,
                                val auth0Client: Auth0Client) : BaseFilter() {

    /**
     * Perform filtering.
     *
     */
    override fun doFilterInternal(req: HttpServletRequest, resp: HttpServletResponse, chain: FilterChain) {
        trace("AuthenticationBasicFilter start")
        val authorization: String? = req.getHeader("Authorization")
        if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
            trace("Found Basic authentication header, parse username and password")
            val base64Credentials = authorization.substring("Basic".length).trim { it <= ' ' }
            val credDecoded: ByteArray = Base64.getDecoder().decode(base64Credentials)
            val credentials = String(credDecoded, StandardCharsets.UTF_8)
            val (username, password) = credentials.split(":".toRegex(), 2).toTypedArray()

            trace("Username: ${username}")
            trace("Password: ${password}")

            val jsonObject = auth0Client.clientCredentialsExchange(username, password, "https://dniel.in")
            val accessToken = jsonObject.get("access_token")
            trace("Access Token: ${accessToken}")
        } else {
            trace("No authentication headers found to basic auth.")
        }
        chain.doFilter(req, resp)
        trace("AuthenticationBasicFilter filter done")
    }

}

