package dniel.forwardauth.domain

import java.net.URI

class OriginUrl(val protocol: String, val host: String, val uri: String) {

    override fun toString(): String {
        return "$protocol://$host$uri"
    }

    fun startsWith(url: String): Boolean = this.toString().startsWith(url)

    fun uri(): URI = URI.create(this.toString())
}