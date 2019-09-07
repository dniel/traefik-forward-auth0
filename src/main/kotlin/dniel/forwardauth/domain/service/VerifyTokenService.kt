package dniel.forwardauth.domain.service

import com.auth0.jwt.interfaces.DecodedJWT
import dniel.forwardauth.domain.InvalidToken
import dniel.forwardauth.domain.JwtToken
import dniel.forwardauth.domain.OpaqueToken
import dniel.forwardauth.domain.Token
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.lang.IllegalStateException

/**
 * Interface for decoder of Jwt Tokens that is provided to the VerifyTokenService
 * so that we can mock it in tests and return different kinds of JWTS.
 *
 * @see dniel.forwardauth.infrastructure.auth0.Auth0JwtDecoder
 */
interface JwtDecoder {
    fun verify(token: String, domain: String): DecodedJWT
}

@Component
class VerifyTokenService(val decoder: JwtDecoder) {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    fun verify(token: String?, expectedAudience: String, expectedDomain: String): Token {
        return when {
            // if its a null or empty string just fail fast.
            token.isNullOrEmpty() -> InvalidToken("Missing token")

            // verify that the token has JWT format, eg. three parts separated by .
            isOpaqueToken(token) -> OpaqueToken(token)

            else -> {
                val decodedJWT = decodeToken(token, expectedDomain)
                if (verifyAudience(decodedJWT, expectedAudience)) {
                    LOGGER.debug("Verify audience failed, expected ${expectedAudience} but got ${decodedJWT.audience}")
                    throw IllegalStateException("Verify audience failed, expected ${expectedAudience} but got ${decodedJWT.audience}")
                } else {
                    JwtToken(decodedJWT)
                }
            }
        }
    }

    private fun isOpaqueToken(token: String): Boolean = token.split(".").size == 0

    private fun verifyAudience(decodedJWT: DecodedJWT, expectedAudience: String): Boolean = !decodedJWT.audience.contains(expectedAudience)

    private fun decodeToken(token: String, domain: String): DecodedJWT = decoder.verify(token, domain)
}