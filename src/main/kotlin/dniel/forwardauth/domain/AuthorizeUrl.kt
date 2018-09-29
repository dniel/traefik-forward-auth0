package dniel.forwardauth.domain

import java.net.URI

class AuthorizeUrl(val authorizeUrl: String,
                   val audience: String,
                   val scope: Array<String>,
                   val clientId: String,
                   val redirectUrl: String,
                   val state: State) {

    override fun toString(): String {
        return "$authorizeUrl?audience=$audience&scope=${scope.joinToString("%20")}&response_type=code&client_id=$clientId&redirect_uri=$redirectUrl&state=$state"
    }

    fun toURI(): URI = URI.create(toString())
}