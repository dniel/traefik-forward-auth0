package dniel.forwardauth.application

import com.auth0.jwt.interfaces.Claim
import dniel.forwardauth.AuthProperties
import dniel.forwardauth.AuthProperties.Application
import dniel.forwardauth.domain.*
import dniel.forwardauth.domain.service.Authorizer
import dniel.forwardauth.domain.service.AuthorizerStateMachine
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
        private val LOGGER = LoggerFactory.getLogger(this::class.java)
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
     * Main Handle Command method.
     */
    override fun handle(params: AuthorizeCommand): AuthEvent {
        val app = properties.findApplicationOrDefault(params.host)
        val nonce = nonceService.generate()
        val originUrl = OriginUrl(params.protocol, params.host, params.uri, params.method)
        val state = State.create(originUrl, nonce)
        val authorizeUrl = AuthorizeUrl(AUTHORIZE_URL, app, state).toURI()
        val cookieDomain = app.tokenCookieDomain
        val accessToken = verifyTokenService.verify(params.accessToken, app.audience)
        val idToken = verifyTokenService.verify(params.idToken, app.clientId)

        val authorizer = Authorizer.create(accessToken, idToken, app, nonce, originUrl, state, AuthorizeUrl(AUTHORIZE_URL, app, state), properties.domain)
        val output = authorizer.authorize()
        LOGGER.debug("" + output)
        return when (output) {
            AuthorizerStateMachine.State.NEED_REDIRECT -> AuthEvent.NeedRedirect(authorizeUrl, nonce, cookieDomain)
            AuthorizerStateMachine.State.ACCESS_DENIED -> AuthEvent.AccessDenied
            AuthorizerStateMachine.State.ACCESS_GRANTED -> AuthEvent.AccessGranted(getUserinfoFromToken(app, idToken as JwtToken))
            else -> AuthEvent.Error
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

    /**
     * This command can produce a set of events as response from the handle method.
     */
    sealed class AuthEvent : Event {
        class NeedRedirect(val authorizeUrl: URI, val nonce: Nonce, val cookieDomain: String) : AuthEvent()
        class AccessGranted(val userinfo: Map<String, String>) : AuthEvent()
        object AccessDenied : AuthEvent()
        object Error : AuthEvent()
    }

}