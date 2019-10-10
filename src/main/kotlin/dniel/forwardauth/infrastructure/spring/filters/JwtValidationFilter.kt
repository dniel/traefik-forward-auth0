package dniel.forwardauth.infrastructure.spring.filters

import dniel.forwardauth.application.AuthenticateHandler
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Validate Tokens and if valid, set as authenticated user.
 */
@Component
class JwtValidationFilter(val authenticateHandler: AuthenticateHandler) : OncePerRequestFilter() {
    override fun doFilterInternal(req: HttpServletRequest, resp: HttpServletResponse, chain: FilterChain) {
        if (req.cookies != null) {
            val accessToken = readCookie(req, "ACCESS_TOKEN")
            val idToken = readCookie(req, "JWT_TOKEN")
            val host = req.getHeader("x-forwarded-host")

            val event = authenticateHandler.handle(AuthenticateHandler.AuthenticateCommand(accessToken, idToken, host)) as AuthenticateHandler.AuthEvent
            if (event is AuthenticateHandler.AuthEvent.AuthenticatedUserEvent) {
                try {
                    val token = event.accessToken
                    val sub = token.subject()
                    val permissions = token.permisssions()
                    val grantedAuthority = permissions.map { permission -> SimpleGrantedAuthority(permission) }
                    val auth = UsernamePasswordAuthenticationToken(sub, null, grantedAuthority)
                    SecurityContextHolder.getContext().authentication = auth
                } catch (e: Exception) {
                    println("got excpetion")
                    println("e: " + e.message)
                    SecurityContextHolder.clearContext()
                }
            }
        }
        chain.doFilter(req, resp)
    }

    fun readCookie(req: HttpServletRequest, key: String): String? {
        return req.cookies.filter { c -> key.equals(c.getName()) }.map { cookie -> cookie.value }.firstOrNull()
    }
}

