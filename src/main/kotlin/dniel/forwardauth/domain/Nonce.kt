package dniel.forwardauth.domain

import java.util.*

class Nonce private constructor(val value: String) {

    companion object {
        fun create(): Nonce {
            val uuid = UUID.randomUUID()
            return Nonce(uuid.toString().replace("-", ""))
        }
    }

    override fun toString(): String = value

}