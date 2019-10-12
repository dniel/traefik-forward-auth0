package dniel.forwardauth.domain.shared

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils

class User(val accessToken: JwtToken, val idToken: JwtToken, val userinfo: Map<String, String>) : org.springframework.security.core.userdetails.UserDetails {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return AuthorityUtils.createAuthorityList(*accessToken.permisssions())
    }

    override fun isEnabled(): Boolean = true
    override fun getUsername(): String = accessToken.subject()
    override fun isCredentialsNonExpired(): Boolean = true
    override fun getPassword(): String = ""
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true

}