package dniel.forwardauth.infrastructure.auth0

import com.auth0.jwk.GuavaCachedJwkProvider
import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.RSAKeyProvider
import dniel.forwardauth.AuthProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

@Component
class Auth0Utils(val properties: AuthProperties) {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    val DOMAIN = properties.domain;
    val provider = GuavaCachedJwkProvider(UrlJwkProvider(DOMAIN))


    fun verifyJWT(token: String): DecodedJWT {
        val jwk = provider.get(JWT.decode(token).keyId)
        val keyProvider = object : RSAKeyProvider {
            override fun getPublicKeyById(kid: String): RSAPublicKey = jwk.publicKey as RSAPublicKey
            override fun getPrivateKey(): RSAPrivateKey? = null
            override fun getPrivateKeyId(): String? = null
        }
        val algorithm = Algorithm.RSA256(keyProvider)
        val verifier = createJwtVerifier(algorithm)
        val decodedJwt = verifier.verify(token)
        return decodedJwt
    }

    private fun createJwtVerifier(algorithm: Algorithm?): JWTVerifier = JWT.require(algorithm)
            .withIssuer(DOMAIN)
            .acceptLeeway(10)
            .build()
}