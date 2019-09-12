package dniel.forwardauth.application

import com.auth0.jwt.interfaces.Claim
import dniel.forwardauth.AuthProperties
import dniel.forwardauth.AuthProperties.Application
import dniel.forwardauth.domain.*
import dniel.forwardauth.domain.service.NonceGeneratorService
import dniel.forwardauth.domain.service.VerifyTokenService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URI


/**
 * Handle Authorization.
 * This command handler will do all the checking if a user has access or not to a url.
 * As a result of all evaluations in the authorization logic the result will be a set
 * of AuthEvents that will be returned as the result from the handle method.
 * <p/>
 * The handle-method will take all the input and verify according to a set of rules
 * if the user has access the requested url. The return list of AuthEvents will contain
 * both successful events an error events from the access logic.
 *
 * <p/>
 * Ideas to error handling
 * http://www.douevencode.com/articles/2018-09/kotlin-error-handling/
 * https://medium.com/@spaghetticode/finite-state-machines-in-kotlin-part-1-57e68d54d93b
 */
@Component
class AuthorizeHandler(val properties: AuthProperties,
                       val verifyTokenService: VerifyTokenService,
                       val nonceService: NonceGeneratorService) : CommandHandler<AuthorizeHandler.AuthorizeCommand> {

    private val AUTHORIZE_URL = properties.authorizeUrl
    private val AUTH_DOMAIN = properties.domain

    companion object {
        private val LOGGER = LoggerFactory.getLogger(this.javaClass)
    }

    /**
     * This is the parameter object for the handler to pass inn all
     * needed parameters to the handler.
     */
    data class AuthorizeCommand(val accessToken: String?,
                                val idToken: String?,
                                val protocol: String,
                                val host: String,
                                val uri: String,
                                val method: String
    ) : Command

    /**
     * This command can produce a set of events as response from the handle method.
     */
    sealed class AuthEvent : Event {
        abstract class NeedRedirectEvent(val authorizeUrl: URI, val nonce: Nonce, val cookieDomain: String) : AuthEvent()
        class IllegalAccessTokenEvent(authorizeUrl: URI, nonce: Nonce, cookieDomain: String) : NeedRedirectEvent(authorizeUrl, nonce, cookieDomain)
        class IllegalIdTokenEvent(authorizeUrl: URI, nonce: Nonce, cookieDomain: String) : NeedRedirectEvent(authorizeUrl, nonce, cookieDomain)

        abstract class PermissionDeniedEvent : AuthEvent()
        class MissingPermissionsEvent(val reason: String) : PermissionDeniedEvent()
        object IllegalMethodEvent : PermissionDeniedEvent()

        object ValidPermissionsEvent : AuthEvent()
        object ValidAccessTokenEvent : AuthEvent()
        class ValidIdTokenEvent(val userinfo: Map<String, String>) : AuthEvent()
        object ValidSignInEvent : AuthEvent()
        object ValidMethodEvent : AuthEvent()
    }

    /**
     * To handle authorization checks in a structured way each check
     * has been implemented as an Authentication Rule, each rule produces and
     * event as a response and all the events are returned as a result out of
     * command handler.
     */
    abstract class AuthRule(val context: Map<String, Any>) {
        abstract fun verify(params: AuthorizeCommand): AuthEvent?
    }

    /**
     * Verify that its a valid and verified JWT token, and if required permissions
     * has been configured for the application check that the permissions set in
     * the token matches all required permissions set for the application.
     *
     * If the token is Opaque its not possible in Auth0 to set required permissions
     * the application configured in ForwardAuth cant have required permissions set
     * to avoid confusion. If opaque token and no permissions required in config
     * everything is ok and access granted.
     */
    class VerifyHasPermission(context: Map<String, Any>) : AuthRule(context) {
        override fun verify(params: AuthorizeCommand): AuthEvent? {
            val app = context.get("app") as Application
            val accessToken = context.get("access_token") as Token
            val authorizeUrl = context.get("authorize_url") as URI
            val nonce = context.get("nonce") as Nonce
            val cookieDomain = context.get("cookie_domain") as String

            return when {
                accessToken is InvalidToken -> AuthEvent.IllegalAccessTokenEvent(authorizeUrl, nonce, cookieDomain)
                accessToken is JwtToken && accessToken.hasPermission(app.requiredPermissions) -> AuthEvent.ValidPermissionsEvent
                accessToken is OpaqueToken && app.requiredPermissions.isNullOrEmpty() -> AuthEvent.ValidPermissionsEvent
                else -> AuthEvent.MissingPermissionsEvent("Required permissions to access: " + app.requiredPermissions.joinToString(","))
            }
        }
    }

    /**
     * The Signin Request url should not be protected.
     * If the signin redirect url was protected no-one would be able to sign in and be stuck in
     * sign in loop.
     */
    class VerifyAllowSignInRequest(context: Map<String, Any>) : AuthRule(context) {
        override fun verify(params: AuthorizeCommand): AuthEvent? {
            val app = context.get("app") as Application
            val originUrl = context.get("origin_url") as OriginUrl

            return if (isSigninUrl(originUrl, app)) {
                AuthEvent.ValidSignInEvent
            } else {
                null
            }
        }

        private fun isSigninUrl(originUrl: OriginUrl, app: Application) =
                originUrl.startsWith(app.redirectUri)
    }

    /**
     * Verify if the origin url request has a http-method that was restricted/protected.
     */
    class VerifyRestrictedMethod(context: Map<String, Any>) : AuthRule(context) {
        override fun verify(params: AuthorizeCommand): AuthEvent? {
            val app = context.get("app") as Application
            val originUrl = context.get("origin_url") as OriginUrl
            val method = originUrl.method
            val accessToken = context.get("access_token") as Token

            return if (isRestrictedUrl(app, method) && accessToken is InvalidToken) {
                AuthEvent.IllegalMethodEvent
            } else {
                AuthEvent.ValidMethodEvent
            }
        }

        private fun isRestrictedUrl(app: Application, method: String) =
                app.restrictedMethods.any() { t -> t.equals(method, true) }
    }

    /**
     * Verify that the Access Token was decoded and verified as correct and not tampered with token.
     * The code as checked that the singature, audience, domain is as expected and that the the token has not expired.
     */
    class VerifyValidAccessToken(context: Map<String, Any>) : AuthRule(context) {
        override fun verify(params: AuthorizeCommand): AuthEvent? {
            val token = context.get("access_token") as Token
            val authorizeUrl = context.get("authorize_url") as URI
            val nonce = context.get("nonce") as Nonce
            val cookieDomain = context.get("cookie_domain") as String

            return when {
                token is JwtToken -> AuthEvent.ValidAccessTokenEvent
                else -> AuthEvent.IllegalAccessTokenEvent(authorizeUrl, nonce, cookieDomain)
            }
        }
    }

    /**
     * Verify that the Access Token was decoded and verified as correct and not tampered with token.
     * The code as checked that the singature, audience, domain is as expected and that the the token has not expired.
     */
    class VerifyValidIdToken(context: Map<String, Any>) : AuthRule(context) {
        override fun verify(params: AuthorizeCommand): AuthEvent? {
            val app = context.get("app") as Application
            val token = context.get("id_token") as Token
            val authorizeUrl = context.get("authorize_url") as URI
            val nonce = context.get("nonce") as Nonce
            val cookieDomain = context.get("cookie_domain") as String

            return when {
                token is JwtToken -> AuthEvent.ValidIdTokenEvent(getUserinfoFromToken(app, token))
                else -> AuthEvent.IllegalIdTokenEvent(authorizeUrl, nonce, cookieDomain)
            }
        }

        private fun getUserinfoFromToken(app: Application, token: JwtToken): Map<String, String> {
            app.claims.forEach { s -> LOGGER.trace("Should add Claim from token: ${s}") }
            return token.value.claims
                    .onEach { entry: Map.Entry<String, Claim> -> LOGGER.trace("Token Claim ${entry.key}=${getClaimValue(entry.value)}") }
                    .filterKeys { app.claims.contains(it) }
                    .onEach { entry: Map.Entry<String, Claim> -> LOGGER.trace("Filtered claim ${entry.key}=${getClaimValue(entry.value)}") }
                    .mapValues { getClaimValue(it.value) }
                    .filterValues { it != null } as Map<String, String>
        }

        private fun getClaimValue(claim: Claim): String? {
            return when {
                claim.asArray(String::class.java) != null -> claim.asArray(String::class.java).joinToString()
                claim.asBoolean() != null -> claim.asBoolean().toString()
                claim.asString() != null -> claim.asString().toString()
                claim.asLong() != null -> claim.asLong().toString()
                else -> null
            }
        }
    }

    /**
     * Main Handle Command method.
     */
    override fun handle(params: AuthorizeCommand): List<AuthEvent> {
        val context = createAuthRuleContext(params)
        val rules = listOf<AuthRule>(
                VerifyAllowSignInRequest(context),
                VerifyRestrictedMethod(context),
                VerifyHasPermission(context),
                VerifyValidAccessToken(context),
                VerifyValidIdToken(context))

        val events = rules.foldRight(mutableListOf<AuthEvent>()) { rule, acc ->
            rule.verify(params)?.let {
                acc.add(it)
            }
            acc
        }
        return events
    }

    private fun createAuthRuleContext(params: AuthorizeCommand): MutableMap<String, Any> {
        val app = properties.findApplicationOrDefault(params.host)
        val nonce = nonceService.generate()
        val originUrl = OriginUrl(params.protocol, params.host, params.uri, params.method)
        val state = State.create(originUrl, nonce)
        val authorizeUrl = AuthorizeUrl(AUTHORIZE_URL, app, state).toURI()
        val cookieDomain = app.tokenCookieDomain

        LOGGER.debug("Authorize request=${originUrl} to app=${app.name}")
        val context = emptyMap<String, Any>().toMutableMap()

        val accessToken = verifyTokenService.verify(params.accessToken, app.audience, AUTH_DOMAIN)
        val idToken = verifyTokenService.verify(params.idToken, app.clientId, AUTH_DOMAIN)

        context.put("access_token", accessToken)
        context.put("id_token", idToken)
        context.put("app", app)
        context.put("nonce", nonce)
        context.put("origin_url", originUrl)
        context.put("state", state)
        context.put("authorize_url", authorizeUrl)
        context.put("cookie_domain", cookieDomain)
        context.put("auth_domain", AUTH_DOMAIN)
        return context
    }
}