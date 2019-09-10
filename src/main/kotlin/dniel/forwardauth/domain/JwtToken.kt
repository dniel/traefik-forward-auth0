package dniel.forwardauth.domain

import com.auth0.jwt.interfaces.DecodedJWT
import org.slf4j.LoggerFactory

class JwtToken(val value: DecodedJWT) : Token {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    fun hasPermission(requiredPermissions: Array<String>): Boolean {
        // if required permissions list is empty any permission is allowed.
        if (requiredPermissions.isEmpty()) return true

        val claim = value.getClaim("permissions")
        return claim.run {
            asArray(String::class.java)
        }.run {
            val hasPermission = asList().containsAll(requiredPermissions.asList())
            LOGGER.trace("Required permissions: [${requiredPermissions.joinToString()}]")
            LOGGER.trace("User permissions: [${asList().joinToString()}]")
            hasPermission
        }
    }
}