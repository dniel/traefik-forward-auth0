package dniel.forwardauth.domain.service

import com.auth0.jwt.interfaces.DecodedJWT
import dniel.forwardauth.domain.Token
import dniel.forwardauth.infrastructure.jwt.JwtDecoder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Response

@Component
class VerifyTokenService(val decoder: JwtDecoder) {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    fun verify(token: String, expectedAudience: String, domain: String): Token {
        val decodedJWT = decodeToken(token, domain)
        return Token(verifyAudience(decodedJWT, expectedAudience))
    }

    private fun decodeToken(token: String, domain: String): DecodedJWT {
        val decodedJWT = decoder.verify(token, domain)
        return decodedJWT
    }

    fun verifyAudience(decodedJWT: DecodedJWT, expectedAudience: String): DecodedJWT {
        if (!decodedJWT.audience.contains(expectedAudience)) {
            // TODO should not just return the token if the audience is wrong.
            LOGGER.error("VerifyAudience Failed to verify audience: expected=$expectedAudience, actual=${decodedJWT.audience}");
            return decodedJWT
        }

        return decodedJWT
    }
}