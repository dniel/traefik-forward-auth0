package dniel.forwardauth.domain

import dniel.forwardauth.AuthProperties
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class AuthorizeUrl(val authorizeUrl: String,
                   val app: AuthProperties.Application,
                   val state: State) {

    override fun toString(): String {
        val scopes = URLEncoder.encode(app.scope, StandardCharsets.UTF_8.toString());
        val audience = URLEncoder.encode(app.audience, StandardCharsets.UTF_8.toString());
        val redirectUri = URLEncoder.encode(app.redirectUri, StandardCharsets.UTF_8.toString());
        return "$authorizeUrl?audience=${audience}&scope=${scopes}&response_type=code&client_id=${app.clientId}&redirect_uri=${redirectUri}&state=$state"
    }

    fun toURI(): URI = URI.create(toString())
}