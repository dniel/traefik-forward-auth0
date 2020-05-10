package dniel.forwardauth.infrastructure.spring.filters

import com.google.common.cache.CacheBuilder
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
import java.util.concurrent.TimeUnit
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

    // Hold already retrived tokens in cache for client_credentials to avoid excessive api calls to Auth0.
    val cache = CacheBuilder.newBuilder().expireAfterAccess(24, TimeUnit.HOURS).build<Int, CachedToken>()

    /**
     * Object to cache, contains both the received token and when it expires for refresh.
     * @param accessToken is the token that is cached
     * @param expires is milliseconds since epoch for when the token expires.
     */
    data class CachedToken(val accessToken: String, val expires: Long) {
        fun hasExpired(): Boolean = System.currentTimeMillis() > expires
    }

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

                // retrieve token from cache, or if not found request one from Auth0.
                var cachedToken = retrieveToken(credentials, host, username, password)

                // if cached token has expired, invalidate it and reload from Auth0.
                if (cachedToken.hasExpired()) {
                    trace("Token has expired in cache, invalidate and request a new one from Auth0.")
                    cache.invalidate(cachedToken)
                    cachedToken = retrieveToken(credentials, host, username, password)
                }

                // authorize user, put resulting user in SecurityContextHolder
                // when using client_credentials you dont have any id token
                // so just send null as token.
                authorize(cachedToken.accessToken, null, host)
                MDC.put("userId", SecurityContextHolder.getContext().authentication.name)
            } else {
                trace("No authentication headers found to basic auth.")
            }
        }
        chain.doFilter(req, resp)
        trace("AuthenticationBasicFilter filter done")
    }

    private fun retrieveToken(credentials: String, host: String?, username: String, password: String): CachedToken {
        val cachedToken = cache.get(credentials.hashCode()) {
            // call Auth0 and retrieve access token.
            trace("Request access token by client credentials from Auth0.")
            val applicationOrDefault = properties.findApplicationOrDefault(host)
            val jsonObject = auth0Client.clientCredentialsExchange(username, password, applicationOrDefault.audience)
            val accessToken = jsonObject.getString("access_token")
            val expiresIn = jsonObject.getInt("expires_in")

            CachedToken(accessToken, System.currentTimeMillis() + (expiresIn * 1000))
        }
        return cachedToken
    }

}

