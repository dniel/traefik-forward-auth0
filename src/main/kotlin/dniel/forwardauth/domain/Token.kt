package dniel.forwardauth.domain

import com.auth0.jwk.GuavaCachedJwkProvider
import com.auth0.jwk.JwkProvider
import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.RSAKeyProvider
import org.slf4j.LoggerFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Response


class Token private constructor(val value: DecodedJWT) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(this.javaClass)
        var provider: JwkProvider? = null

        fun verify(token: String, expectedAudience: String, domain: String): Token {
            this.provider = GuavaCachedJwkProvider(UrlJwkProvider(domain))
            val decodedJWT = verifyJWT(token, domain)
            verifyAudience(decodedJWT, expectedAudience)
            return Token(decodedJWT)
        }

        private fun verifyJWT(token: String, domain: String): DecodedJWT {
            try {
                val decodedJWT = JWT.decode(token)
                val jwk = provider!!.get(decodedJWT.keyId)
                val keyProvider = object : RSAKeyProvider {
                    override fun getPublicKeyById(kid: String): RSAPublicKey = jwk.publicKey as RSAPublicKey
                    override fun getPrivateKey(): RSAPrivateKey? = null
                    override fun getPrivateKeyId(): String? = null
                }
                val algorithm = Algorithm.RSA256(keyProvider)
                val verifier = createJwtVerifier(algorithm, domain)
                val decodedJwt = verifier.verify(token)

                return decodedJwt
            } catch (e: Exception) {
                LOGGER.error("Failed to verify access token", e);
                throw WebApplicationException("Failed to verify access token", Response.Status.BAD_REQUEST)
            }
        }

        private fun createJwtVerifier(algorithm: Algorithm?, domain: String): JWTVerifier = JWT.require(algorithm)
                .withIssuer(domain)
                .acceptLeeway(10)
                .build()

        private fun verifyAudience(decodedJWT: DecodedJWT, expectedAudience: String) {
            if (decodedJWT.audience.contains(expectedAudience)) {
                LOGGER.debug("Token has valid audience: $expectedAudience");
            } else {
                LOGGER.error("Failed to verify audience: expected=$expectedAudience, actual=${decodedJWT.audience}");
                throw WebApplicationException("Failed to verify audience: expected=$expectedAudience, actual=${decodedJWT.audience}", Response.Status.BAD_REQUEST)
            }
        }

    }
}