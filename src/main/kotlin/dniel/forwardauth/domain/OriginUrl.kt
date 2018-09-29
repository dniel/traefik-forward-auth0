package dniel.forwardauth.domain

import java.net.URI

class OriginUrl(val forwardedProto: String, val forwardedHost: String, val forwardedUri: String) {

    override fun toString(): String {
        return "$forwardedProto://$forwardedHost$forwardedUri"
    }

    fun startsWith(url: String): Boolean = this.toString().startsWith(url)

    fun uri(): URI = URI.create(this.toString())
}