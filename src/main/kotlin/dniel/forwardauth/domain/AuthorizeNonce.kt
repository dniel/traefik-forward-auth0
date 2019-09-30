package dniel.forwardauth.domain

import java.util.*

/**
 * The nonce parameter value needs to include per-session authorizeState and be unguessable to attackers.
 *
 * One method to achieve this for Web Server Clients is to store a cryptographically random value
 * as an HttpOnly session cookie and use a cryptographic hash of the value as the nonce parameter.
 * In that case, the nonce in the returned ID Token is compared to the hash of the session cookie to
 * detect ID Token replay by third parties. A related method applicable to JavaScript Clients is to
 * store the cryptographically random value in HTML5 local storage and use a cryptographic hash of this value.
 *
 * https://openid.net/specs/openid-connect-core-1_0.html#NonceNotes
 * https://tools.ietf.org/html/draft-ietf-oauth-v2-threatmodel-06#section-4.6.2
 * https://tools.ietf.org/html/draft-ietf-oauth-v2-threatmodel-06#section-3.6
 */
class AuthorizeNonce(val value: String) {

    companion object {
        fun generate(): AuthorizeNonce {
            return AuthorizeNonce(UUID.randomUUID().toString().replace("-", ""))
        }

    }
    override fun toString(): String = value
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AuthorizeNonce

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }


}