package dniel.forwardauth.domain

import com.auth0.jwt.interfaces.DecodedJWT
import org.slf4j.LoggerFactory

class JwtToken(val value: DecodedJWT) : Token {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    fun subject(): String = value.subject

    fun hasPermission(requiredPermissions: Array<String>): Boolean {
        // if required permissions list is empty any permission is allowed.
        if (requiredPermissions.isEmpty()) return true

        // if access token doesnt have the permissions field set, allow everything.
        val claim = value.getClaim("permissions")
        if(claim.isNull) return true

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