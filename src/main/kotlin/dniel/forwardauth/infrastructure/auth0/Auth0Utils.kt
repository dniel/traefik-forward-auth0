/*
 * Copyright (c)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dniel.forwardauth.infrastructure.auth0

import com.auth0.jwk.GuavaCachedJwkProvider
import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.RSAKeyProvider
import dniel.forwardauth.infrastructure.micronaut.config.ApplicationConfig
import jakarta.inject.Singleton
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import org.slf4j.LoggerFactory

@Singleton
class Auth0Utils(val properties: ApplicationConfig) {
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