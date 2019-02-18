package dniel.forwardauth.infrastructure.jwt

import com.auth0.jwk.GuavaCachedJwkProvider
import com.auth0.jwk.JwkProvider
import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.RSAKeyProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Response

interface JwtDecoder {
    fun verify(token: String, domain: String): DecodedJWT
}

@Component
class JwtDecoderImpl : JwtDecoder {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)
    var provider: JwkProvider? = null

    override fun verify(token: String, domain: String): DecodedJWT {
        this.provider = GuavaCachedJwkProvider(UrlJwkProvider(domain))
        return verifyJWT(token, domain)
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
            val verifiedJwt = verifier.verify(token)
            return verifiedJwt
        } catch (e: Exception) {
            val message = e.message
            LOGGER.error("Failed to verify token, ${message}", e);
            throw WebApplicationException("Failed to verify token, ${message}", Response.Status.BAD_REQUEST)
        }
    }

    private fun createJwtVerifier(algorithm: Algorithm?, domain: String): JWTVerifier = JWT.require(algorithm)
            .withIssuer(domain)
            .acceptLeeway(10)
            .build()

}