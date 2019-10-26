package dniel.forwardauth.domain.shared

import com.auth0.jwt.interfaces.DecodedJWT
import org.slf4j.LoggerFactory

/**
 * Generic Token
 * A token can be of different types,
 * <li>InvalidToken
 * <li>JwtToken
 * <li>OpaqueToken
 */
sealed class Token

/**
 * A JWT token that was parsed and found invalid.
 */
class InvalidToken(val reason: String) : Token()

/**
 * An OpaqueToken is a token that didnt follow the format of a JWT token
 * Most probably this is a random string without any meaning.
 */
class OpaqueToken(val value: String) : Token()

/**
 * A JWT token that has been parsed and found valid.
 * This class represents a token that does follow the compact format of a JWT token.
 * In its compact form, JSON Web Tokens consist of three parts separated by dots (.), which are:
 *
 * <li>Header
 * <li>Payload
 * <li>Signature
 *
 * Therefore, a JWT typically looks like the following.
 * header.payload.signature
 *
 * @link https://jwt.io/introduction/
 */
class JwtToken(val value: DecodedJWT) : Token() {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    fun subject(): String = value.subject

    fun hasPermission(requiredPermissions: Array<String>): Boolean {
        // if required permissions list is empty any permission is allowed.
        if (requiredPermissions.isEmpty()) return true

        // if access token doesnt have the permissions field set,
        // and the required permissions is not set, or empty, allow everybody to access.
        val claim = value.getClaim("permissions")
        if (claim.isNull && requiredPermissions.isNullOrEmpty()) return true

        return claim.run {
            asArray(String::class.java)
        }.run {
            val hasPermission = asList().containsAll(requiredPermissions.asList())
            val missing = requiredPermissions.asList().minus(asList())
            LOGGER.trace("Missing permissions: $missing")
            LOGGER.trace("Required permissions: ${requiredPermissions.asList()}")
            LOGGER.trace("User permissions: ${asList()}")
            hasPermission
        }
    }

    /**
     * read permissions claim from token and convert to array
     */
    fun permisssions(): Array<String> {
        val claim = value.getClaim("permissions")
        return if (claim.isNull) emptyArray()
        else claim.asArray(String::class.java)
    }

    fun hasPermissionClaim(): Boolean {
        return !value.getClaim("permissions").isNull
    }


    fun missingPermissions(requiredPermissions: Array<String>): Array<String> {
        val claim = value.getClaim("permissions")
        if (claim.isNull) return emptyArray()

        val permissions = claim.asList(String::class.java)
        return requiredPermissions.asList().minus(permissions).toTypedArray()
    }

}