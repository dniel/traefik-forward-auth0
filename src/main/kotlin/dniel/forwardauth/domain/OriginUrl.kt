package dniel.forwardauth.domain

import java.net.URI

class OriginUrl(val protocol: String, val host: String, val uri: String, val method: String) {

    override fun toString(): String {
        return "$protocol://$host$uri".toLowerCase()
    }

    fun startsWith(url: String): Boolean = this.toString().startsWith(url, ignoreCase = true)

    fun uri(): URI = URI.create(this.toString())
}