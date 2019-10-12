package dniel.forwardauth.domain.shared

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils

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
open class Authenticated(override val accessToken: Token,
                         override val idToken: Token,
                         val userinfo: Map<String, String>) : org.springframework.security.core.userdetails.UserDetails, User {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return AuthorityUtils.createAuthorityList(*(accessToken as JwtToken).permisssions())
    }

    override fun isEnabled(): Boolean = true
    override fun getUsername(): String = (accessToken as JwtToken).subject()
    override fun isCredentialsNonExpired(): Boolean = true
    override fun getPassword(): String = ""
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true

}

/**
 * Anonymous user object.
 */
object Anonymous : User {
    override val accessToken: Token
        get() = InvalidToken("anonymous")
    override val idToken: Token
        get() = InvalidToken("anonymous")
}