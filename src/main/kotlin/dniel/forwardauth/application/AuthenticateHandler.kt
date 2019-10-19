package dniel.forwardauth.application

import com.auth0.jwt.interfaces.Claim
import dniel.forwardauth.domain.shared.Application
import dniel.forwardauth.AuthProperties
import dniel.forwardauth.domain.authorize.service.Authenticator
import dniel.forwardauth.domain.authorize.service.AuthenticatorStateMachine
import dniel.forwardauth.domain.events.Event
import dniel.forwardauth.domain.shared.*
import dniel.forwardauth.domain.shared.service.VerifyTokenService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handle Authentication.
 * This class handles authentication of a user and will produce a result of either an authenticated user
 * or an anonymous user. The logic for how to perform an authentication is separated out to the
 * state machine in its own service to be maintainable.
 *
 * @see dniel.forwardauth.domain.authorize.service.AuthenticatorStateMachine
 * @see dniel.forwardauth.domain.authorize.service.Authenticator
 *
 */
@Component
class AuthenticateHandler(val properties: AuthProperties,
                          val verifyTokenService: VerifyTokenService) : CommandHandler<AuthenticateHandler.AuthenticateCommand> {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    /**
     * This is the input parameter object for the handler to pass inn all
     * needed parameters to the handler.
     */
    data class AuthenticateCommand(val accessToken: String?,
                                   val idToken: String?,
                                   val host: String
    ) : Command


    /**
     * This command can produce a set of events as response from the handle method.
     */
    sealed class AuthentiationEvent(user: User) : Event(user) {
        class AuthenticatedEvent(user: User) : AuthentiationEvent(user)
        class AnonymousUserEvent() : AuthentiationEvent(Anonymous)
        class Error(error: Authenticator.Error?) : AuthentiationEvent(Anonymous) {
            val reason: String = error?.message ?: "Unknown error"
        }
    }

    /**
     * Main handle Authentication method.
     * This method will parse all input parameters from the command, create a new
     * instance of the Authenticator service to perform the authentication logic.
     * <p/>
     * The result will be either an Autheniticated user or an Anonymous user.
     */
    override fun handle(params: AuthenticateHandler.AuthenticateCommand): Event {
        val app = properties.findApplicationOrDefault(params.host)
        val accessToken = verifyTokenService.verify(params.accessToken, app.audience)
        val idToken = verifyTokenService.verify(params.idToken, app.clientId)

        val authenticator = Authenticator.create(accessToken, idToken, app)
        val (state, error) = authenticator.authenticate()

        LOGGER.debug("State: ${state}")
        LOGGER.debug("Error: ${error}")

        return when (state) {
            AuthenticatorStateMachine.State.ANONYMOUS -> AuthentiationEvent.AnonymousUserEvent()
            AuthenticatorStateMachine.State.AUTHENTICATED -> {
                val user = Authenticated(accessToken as JwtToken, idToken as JwtToken, getUserinfoFromToken(app, idToken as JwtToken))
                AuthentiationEvent.AuthenticatedEvent(user)
            }
            else -> AuthentiationEvent.Error(error)
        }
    }

    /**
     * Get selected userinfo from token claims.
     */
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