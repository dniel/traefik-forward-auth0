package dniel.forwardauth.infrastructure.spring.services

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class UserDetailService : UserDetailsService {
    override fun loadUserByUsername(p0: String?): UserDetails {
        TODO("not implemented")
    }
}