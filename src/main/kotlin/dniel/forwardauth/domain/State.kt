package dniel.forwardauth.domain

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.*


class State private constructor(val originUrl: OriginUrl, val nonce: Nonce) {

    companion object {
        val JSON = jacksonObjectMapper()
        fun create(originUrl: OriginUrl, nonce: Nonce): State {
            return State(originUrl = originUrl, nonce = nonce)
        }

        fun decode(base64: String): State {
            return JSON.readValue(Base64.getDecoder().decode(base64), State::class.java)
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