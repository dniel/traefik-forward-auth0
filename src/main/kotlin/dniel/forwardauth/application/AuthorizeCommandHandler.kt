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

@Component
class AuthorizeCommandHandler(val properties: AuthProperties,
                              val verifyTokenService: VerifyTokenService,
                              val nonceService: NonceGeneratorService) {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)
    private val AUTHORIZE_URL = properties.authorizeUrl
    private val DOMAIN = properties.domain

    /**
     * this is the parameter object for the handler to pass inn all
     * needed parameters by the handler.
     */
    public data class AuthorizeCommand(val accessToken: String?,
                                       val idToken: String?,
                                       val protocol: String,
                                       val host: String,
                                       val uri: String,
                                       val method: String
    )


    /**
     * The result from the Authorization, all return values.
     */
    public data class AuthorizeResult(var cookieDomain: String? = null,
                                      var isAuthenticated: Boolean = false,
                                      var isRestrictedUrl: Boolean = true,
                                      var redirectUrl: URI? = null,
                                      var nonce: Nonce? = null,
                                      var userinfo: Map<String, String> = emptyMap()
    )

    fun perform(params: AuthorizeCommand): AuthorizeResult {
        LOGGER.debug("AuthorizeCommandHandler start")
        val commandResult = AuthorizeResult()
        commandResult.run{
            val app = properties.findApplicationOrDefault(params.host)
            val originUrl = OriginUrl(params.protocol, params.host, params.uri)

            cookieDomain = app.tokenCookieDomain
            isAuthenticated = verifyTokens(params, app, commandResult)
            isRestrictedUrl = isRestrictedUrl(originUrl, app)
            if (isRestrictedUrl && !isAuthenticated) {
                val generatedNonce = nonceService.generate()
                val state = State.create(originUrl, generatedNonce)
                val authorizeUrl = AuthorizeUrl(AUTHORIZE_URL, app, state)
                redirectUrl = authorizeUrl.toURI()
                nonce = generatedNonce
            }
        }

        return commandResult
    }

    private fun verifyTokens(params: AuthorizeCommand, app: Application, commandResult: AuthorizeResult): Boolean {
        return verifyIdToken(params, app, commandResult) && verifyAccessToken(params, app, commandResult)
    }

    private fun verifyAccessToken(params: AuthorizeCommand, app: Application, commandResult: AuthorizeResult): Boolean {
        if (hasAccessToken(params)) {
            if (shouldVerifyAccessToken(app)) {
                LOGGER.debug("Verify JWT Access Token.")
                return verifyToken(params.accessToken!!, app.audience, DOMAIN) != null
            } else {
                LOGGER.debug("Skip Verification of opaque Access Token.")
                return true
            }
        } else {
            return false
        }
    }

    private fun verifyIdToken(params: AuthorizeCommand, app: Application, commandResult: AuthorizeResult): Boolean {
        if (hasIdToken(params)) {
            LOGGER.debug("Verify JWT IdToken")
            commandResult.userinfo = getUserinfoFromToken(app, verifyToken(params.idToken!!, app.clientId, DOMAIN)!!)
            return verifyToken(params.idToken!!, app.clientId, DOMAIN) != null
        } else {
            return false
        }
    }

    private fun verifyToken(token: String, expectedAudience: String, domain: String): Token? = verifyTokenService.verify(token, expectedAudience, domain)

    private fun hasAccessToken(params: AuthorizeCommand): Boolean = params.accessToken != null

    private fun hasIdToken(params: AuthorizeCommand): Boolean = params.idToken != null

    private fun shouldVerifyAccessToken(app: Application): Boolean = !app.audience.equals("${DOMAIN}userinfo")

    private fun isRestrictedUrl(originUrl: OriginUrl, app: Application): Boolean {
        return !originUrl.startsWith(app.redirectUri) || app.restrictedMethods.contains(originUrl.protocol)
    }


    private fun getUserinfoFromToken(app: Application, token: Token): Map<String, String> {
        return token.value.claims
                .filterKeys { app.claims.contains(it) }
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