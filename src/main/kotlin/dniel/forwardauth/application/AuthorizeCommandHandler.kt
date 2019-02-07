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
     */
    public data class AuthorizeCommand(val accessToken: String?,
                                       val idToken: String?,
                                       val protocol: String,
                                       val host: String,
                                       val uri: String,
                                       val method: String
    )


    /**
     */
    public data class AuthorizeResult(var app: Application,
                                      var accessToken: Token? = null,
                                      var idToken: Token? = null,
                                      var redirectUrl: URI? = null,
                                      var nonce: Nonce? = null,
                                      var userinfo: Map<String, String> = emptyMap()
    )

    fun perform(params: AuthorizeCommand): AuthorizeResult {
        LOGGER.debug("AuthorizeCommandHandler start")
        val app = properties.findApplicationOrDefault(params.host)
        val commandResult = AuthorizeResult(app)

        val originUrl = OriginUrl(params.protocol, params.host, params.uri)
        val nonce = nonceService.generate()
        val state = State.create(originUrl, nonce)
        val authorizeUrl = AuthorizeUrl(AUTHORIZE_URL, app, state)

        try {
            verifyTokens(params, app, commandResult)
            commandResult.userinfo = getUserinfoFromToken(app, commandResult.idToken!!)
        } catch (e: Exception) {
            LOGGER.info("Verification of tokens failed: ${e.message}");
            commandResult.accessToken = null
            commandResult.idToken = null
        }
        if (isRestrictedMethod(originUrl, app) && missingTokens(commandResult)) {
            LOGGER.debug("Redirect to Auth0 authorize-url for Authorization")
            commandResult.redirectUrl = authorizeUrl.toURI()
            commandResult.nonce = nonceService.generate()
        }

        return commandResult
    }

    private fun verifyTokens(params: AuthorizeCommand, app: Application, commandResult: AuthorizeResult) {
        verifyIdToken(params, app, commandResult)
        verifyAccessToken(params, app, commandResult)
    }

    private fun verifyAccessToken(params: AuthorizeCommand, app: Application, commandResult: AuthorizeResult) {
        if (hasAccessToken(params)) {
            if (shouldVerifyAccessToken(app)) {
                LOGGER.debug("Verify JWT Access Token.")
                commandResult.accessToken = verifyToken(params.accessToken!!, app.audience, DOMAIN)
            } else {
                LOGGER.debug("Skip Verification of opaque Access Token.")
            }
        }
    }

    private fun verifyIdToken(params: AuthorizeCommand, app: Application, commandResult: AuthorizeResult) {
        if (hasIdToken(params)) {
            LOGGER.debug("Verify JWT IdToken")
            commandResult.idToken = verifyToken(params.idToken!!, app.clientId, DOMAIN)
        }
    }

    private fun missingTokens(commandResult: AuthorizeResult) =
            (commandResult.idToken == null || commandResult.accessToken == null)

    private fun verifyToken(token: String, expectedAudience: String, domain: String): Token = verifyTokenService.verify(token, expectedAudience, domain)

    private fun hasAccessToken(params: AuthorizeCommand): Boolean = params.accessToken != null

    private fun hasIdToken(params: AuthorizeCommand): Boolean = params.idToken != null

    private fun shouldVerifyAccessToken(app: Application): Boolean = !app.audience.equals("${DOMAIN}userinfo")

    private fun isRestrictedMethod(originUrl: OriginUrl, app: Application): Boolean {
        return !originUrl.startsWith(app.redirectUri) || app.restrictedMethods.contains(originUrl.protocol)
    }


    private fun getUserinfoFromToken(app: Application, token: Token): Map<String, String> {
        return token.value.claims
                .filterKeys { app.claims.contains(it) }
                .mapValues { getClaimValue(it.value) }
                .filterValues { it != null } as Map<String, String>
    }

    private fun getClaimValue(claim: Claim): String? {
        val claimValue = when {
            claim.asArray(String::class.java) != null -> {
                claim.asArray(String::class.java).joinToString()
            }
            claim.asBoolean() != null -> {
                claim.asBoolean().toString()
            }
            claim.asString() != null -> {
                claim.asString().toString()
            }
            claim.asLong() != null -> {
                claim.asLong().toString()
            }
            else -> null
        }
        return claimValue
    }
}



