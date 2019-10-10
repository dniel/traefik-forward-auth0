package dniel.forwardauth.domain.authorize

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.*


class AuthorizeState private constructor(val originUrl: RequestedUrl, val nonce: AuthorizeNonce) {

    companion object {
        val JSON = jacksonObjectMapper()
        fun create(originUrl: RequestedUrl, nonce: AuthorizeNonce): AuthorizeState {
            return AuthorizeState(originUrl = originUrl, nonce = nonce)
        }

        fun decode(base64: String): AuthorizeState {
            return JSON.readValue(Base64.getDecoder().decode(base64), AuthorizeState::class.java)
        }
    }

    fun toJson(): String {
        return JSON.writeValueAsString(this)
    }

    fun encode(): String {
        return Base64.getEncoder().encodeToString(JSON.writeValueAsBytes(this))
    }

    override fun toString(): String {
        return encode()
    }


}