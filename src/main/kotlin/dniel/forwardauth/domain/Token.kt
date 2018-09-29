package dniel.forwardauth.domain

import com.auth0.jwk.GuavaCachedJwkProvider
import com.auth0.jwk.JwkProvider
import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.RSAKeyProvider
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey


class Token private constructor(val value: DecodedJWT) {
    companion object {
        var provider: JwkProvider? = null

        fun verify(token: String, expectedAudience: String, domain: String): Token {
            this.provider = GuavaCachedJwkProvider(UrlJwkProvider(domain))
            val decodedJWT = verifyJWT(token, domain)
            val verifyAudience = verifyAudience(decodedJWT, expectedAudience)
            return Token(decodedJWT)
        }

        private fun verifyJWT(token: String, domain: String): DecodedJWT {
            val jwk = provider!!.get(JWT.decode(token).keyId)
            val keyProvider = object : RSAKeyProvider {
                override fun getPublicKeyById(kid: String): RSAPublicKey = jwk.publicKey as RSAPublicKey
                override fun getPrivateKey(): RSAPrivateKey? = null
                override fun getPrivateKeyId(): String? = null
            }
            val algorithm = Algorithm.RSA256(keyProvider)
            val verifier = createJwtVerifier(algorithm, domain)
            val decodedJwt = verifier.verify(token)
            return decodedJwt
        }

        private fun createJwtVerifier(algorithm: Algorithm?, domain: String): JWTVerifier = JWT.require(algorithm)
                .withIssuer(domain)
                .acceptLeeway(10)
                .build()

        private fun verifyAudience(decodedJWT: DecodedJWT, expectedAudience: String) = decodedJWT.audience.contains(expectedAudience)

    }
}