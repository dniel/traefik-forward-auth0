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
        return Token(verifyAudience(decoder.verify(token, domain), expectedAudience))
    }

    fun verifyAudience(decodedJWT: DecodedJWT, expectedAudience: String): DecodedJWT {
        if (decodedJWT.audience.contains(expectedAudience)) {
            LOGGER.debug("verifyAudience Token has valid audience: expected=$expectedAudience");
        } else {
            LOGGER.error("verifyAudience Failed to verify audience: expected=$expectedAudience, actual=${decodedJWT.audience}");
            throw WebApplicationException("Failed to verify audience: expected=$expectedAudience, actual=${decodedJWT.audience}", Response.Status.BAD_REQUEST)
        }

        return decodedJWT
    }
}