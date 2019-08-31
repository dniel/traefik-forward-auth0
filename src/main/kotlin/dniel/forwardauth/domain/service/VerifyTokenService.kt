package dniel.forwardauth.domain.service

import com.auth0.jwt.interfaces.DecodedJWT
import dniel.forwardauth.domain.Token
import dniel.forwardauth.domain.exceptions.VerifyTokenException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

interface JwtDecoder {
    fun verify(token: String, domain: String): DecodedJWT
}

@Component
class VerifyTokenService(val decoder: JwtDecoder) {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    fun verify(token: String, expectedAudience: String, expectedDomain: String): Token {
        try {
            val decodedJWT = decodeToken(token, expectedDomain)
            return Token(verifyAudience(decodedJWT, expectedAudience))
        } catch (e: Exception) {
            throw VerifyTokenException("VeryTokenFailed ${e.message}")
        }
    }

    fun verifyAudience(decodedJWT: DecodedJWT, expectedAudience: String): DecodedJWT {
        if (!decodedJWT.audience.contains(expectedAudience)) {
            throw VerifyTokenException("VerifyAudienceFailed expected=$expectedAudience, actual=${decodedJWT.audience}")
        }
        return decodedJWT
    }

    private fun decodeToken(token: String, domain: String): DecodedJWT {
        val decodedJWT = decoder.verify(token, domain)
        return decodedJWT
    }
}