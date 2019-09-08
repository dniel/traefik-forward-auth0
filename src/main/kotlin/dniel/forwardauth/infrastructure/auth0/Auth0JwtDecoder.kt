package dniel.forwardauth.infrastructure.auth0

import com.auth0.jwk.GuavaCachedJwkProvider
import com.auth0.jwk.JwkProvider
import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.RSAKeyProvider
import dniel.forwardauth.domain.service.JwtDecoder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey


@Component
class Auth0JwtDecoder : JwtDecoder {
    val LOGGER = LoggerFactory.getLogger(this.javaClass)
    var provider: JwkProvider? = null

    override fun verify(token: String, domain: String): DecodedJWT {
        this.provider = GuavaCachedJwkProvider(UrlJwkProvider(domain))
        return verifyJWT(token, domain)
    }

    private fun verifyJWT(token: String, domain: String): DecodedJWT {
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
    }

    private fun createJwtVerifier(algorithm: Algorithm?, domain: String): JWTVerifier = JWT.require(algorithm)
            .withIssuer(domain)
            .acceptLeeway(10)
            .build()

}