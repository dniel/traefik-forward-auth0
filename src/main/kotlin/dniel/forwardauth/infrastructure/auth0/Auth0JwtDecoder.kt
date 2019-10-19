package dniel.forwardauth.infrastructure.auth0

import com.auth0.jwk.GuavaCachedJwkProvider
import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.RSAKeyProvider
import dniel.forwardauth.AuthProperties
import dniel.forwardauth.domain.shared.service.JwtDecoder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey


@Component
class Auth0JwtDecoder(val properties: AuthProperties) : JwtDecoder {
    val LOGGER = LoggerFactory.getLogger(this.javaClass)
    val AUTH_DOMAIN = properties.domain
    val provider = GuavaCachedJwkProvider(UrlJwkProvider(AUTH_DOMAIN))

    override fun verify(token: String): DecodedJWT {
        return verifyJWT(token, AUTH_DOMAIN)
    }

    private fun verifyJWT(token: String, domain: String): DecodedJWT {
        val decodedJWT = JWT.decode(token)
        val jwk = provider.get(decodedJWT.keyId)
        val keyProvider = object : RSAKeyProvider {
            override fun getPublicKeyById(kid: String): RSAPublicKey = jwk.publicKey as RSAPublicKey
            override fun getPrivateKey(): RSAPrivateKey? = null
            override fun getPrivateKeyId(): String? = null
        }
        val algorithm = Algorithm.RSA256(keyProvider)
        val verifier = createJwtVerifier(algorithm, domain)
        val verifiedJwt = verifier.verify(token)
        return verifiedJwt
    }

    private fun createJwtVerifier(algorithm: Algorithm?, domain: String): JWTVerifier = JWT.require(algorithm)
            .withIssuer(domain)
            .acceptLeeway(10)
            .build()

}