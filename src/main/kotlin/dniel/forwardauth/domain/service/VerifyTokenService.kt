package dniel.forwardauth.domain.service

import com.auth0.jwt.interfaces.DecodedJWT
import com.google.common.cache.CacheBuilder
import dniel.forwardauth.domain.InvalidToken
import dniel.forwardauth.domain.JwtToken
import dniel.forwardauth.domain.OpaqueToken
import dniel.forwardauth.domain.Token
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

/**
 * Interface for decoder of Jwt Tokens that is provided to the VerifyTokenService
 * so that we can mock it in tests and return different kinds of JWTS.
 *
 * @see dniel.forwardauth.infrastructure.auth0.Auth0JwtDecoder
 */
interface JwtDecoder {
    fun verify(token: String): DecodedJWT
}

@Component
class VerifyTokenService(val decoder: JwtDecoder) {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    // Cache to hold already verified Tokens, expire them from cache after 15 minutes of inactivity.
    val cache = CacheBuilder.newBuilder().expireAfterAccess(15, TimeUnit.MINUTES).build<String, DecodedJWT>()

    fun verify(token: String?, expectedAudience: String): Token {
        return when {
            // if its a null or empty string just fail fast.
            token.isNullOrEmpty() -> InvalidToken("Missing token")

            // verify that the token has JWT format, eg. three parts separated by .
            isOpaqueToken(token) -> OpaqueToken(token)

            else -> {
                try {
                    val decodedJWT = cache.get(token, Callable<DecodedJWT> {
                        decodeToken(token)
                    })
                    when {
                        hasIllegalAudience(decodedJWT, expectedAudience) -> throw IllegalStateException("Verify audience failed, expected ${expectedAudience} but got ${decodedJWT.audience}")
                        hasExpired(decodedJWT) -> throw IllegalStateException("Token has expired ${decodedJWT.expiresAt}")
                        else -> JwtToken(decodedJWT)
                    }
                } catch (e: Exception) {
                    // handle errors from get and decode in cache.
                    cache.invalidate(token) // remove token from cache if found illegal token authorizeState.
                    LOGGER.info("Invalid token: ${e.message}")
                    InvalidToken("" + e.message)
                }
            }
        }
    }

    private fun hasExpired(decodedJWT: DecodedJWT): Boolean = decodedJWT.expiresAt.before(Date())

    private fun isOpaqueToken(token: String): Boolean = token.split(".").size == 1

    private fun hasIllegalAudience(decodedJWT: DecodedJWT, expectedAudience: String): Boolean = !decodedJWT.audience.contains(expectedAudience)

    private fun decodeToken(token: String): DecodedJWT = decoder.verify(token)
}