package dniel.forwardauth.domain.service

import com.auth0.jwt.interfaces.DecodedJWT
import dniel.forwardauth.domain.Token
import dniel.forwardauth.infrastructure.jwt.JwtDecoder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class VerifyTokenService(val decoder: JwtDecoder) {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    fun verify(token: String, expectedAudience: String, domain: String): Token {
        try {
            val decodedJWT = decodeToken(token, domain)
            return Token(verifyAudience(decodedJWT, expectedAudience))
        } catch (e: Exception) {
            throw IllegalStateException("VeryTokenFailed ${e.message}", e)
        }
    }

    fun verifyAudience(decodedJWT: DecodedJWT, expectedAudience: String): DecodedJWT {
        if (!decodedJWT.audience.contains(expectedAudience)) {
            throw IllegalStateException("VerifyAudienceFailed expected=$expectedAudience, actual=${decodedJWT.audience}")
        }

        return decodedJWT
    }

    private fun decodeToken(token: String, domain: String): DecodedJWT {
        val decodedJWT = decoder.verify(token, domain)
        return decodedJWT
    }
}