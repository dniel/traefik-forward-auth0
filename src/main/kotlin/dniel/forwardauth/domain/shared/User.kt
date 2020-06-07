package dniel.forwardauth.domain.shared

/**
 * A generic user interface with access token and id token
 */
interface User {
    val accessToken: Token
    val idToken: Token
}

/**
 * Authenticated user object
 */
open class Authenticated(override val accessToken: JwtToken,
                         override val idToken: Token,
                         val userinfo: Map<String, String>) : User {
    val sub: String = accessToken.subject()
    val permissions: Array<String> = accessToken.permissions()
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