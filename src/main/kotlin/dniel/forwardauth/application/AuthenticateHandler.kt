package dniel.forwardauth.application

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.domain.shared.JwtToken
import dniel.forwardauth.domain.shared.VerifyTokenService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

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
    sealed class AuthEvent : Event {
        class AuthenticatedUserEvent(val accessToken: JwtToken, val idToken: JwtToken) : AuthEvent()
        class AnonymousUserEvent() : AuthEvent()
    }


    override fun handle(params: AuthenticateCommand): Event {
        val app = properties.findApplicationOrDefault(params.host)
        val accessToken = verifyTokenService.verify(params.accessToken, app.audience)
        val idToken = verifyTokenService.verify(params.idToken, app.clientId)

        return when {
            accessToken is JwtToken && idToken is JwtToken -> AuthEvent.AuthenticatedUserEvent(accessToken, idToken)
            else -> AuthEvent.AnonymousUserEvent()
        }
    }

}