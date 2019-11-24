package dniel.forwardauth.domain.shared

import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * A generic user interface with access token and id token
 */
interface User {
    @get:JsonIgnore
    val accessToken: Token
    @get:JsonIgnore
    val idToken: Token
}

/**
 * Authenticated user object
 */
open class Authenticated(override val accessToken: JwtToken,
                         override val idToken: JwtToken,
                         val userinfo: Map<String, String>) : User {
    val sub: String = accessToken.subject()
    val permissions: Array<String> = accessToken.permisssions()
    override fun toString(): String = accessToken.subject()
}

/**
 * Anonymous user object.
 */
object Anonymous : User {
    override val accessToken: Token
        get() = InvalidToken("anonymous")
    override val idToken: Token
        get() = InvalidToken("anonymous")

    override fun toString(): String {
        return "anonymous"
    }
}