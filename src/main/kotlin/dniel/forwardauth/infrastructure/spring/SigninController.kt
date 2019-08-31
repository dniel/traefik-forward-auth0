package dniel.forwardauth.infrastructure.spring

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.domain.service.VerifyTokenService
import dniel.forwardauth.infrastructure.auth0.Auth0Client
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RestController

/**
 * Callback Endpoint for Auth0 signin to retrieve JWT token from code.
 * TODO rename to signin
 */
@RestController
class SigninController(val properties: AuthProperties, val auth0Client: Auth0Client, val verifyTokenService: VerifyTokenService) {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)
    private val DOMAIN = properties.domain


}