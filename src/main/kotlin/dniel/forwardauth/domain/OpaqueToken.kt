package dniel.forwardauth.domain

/**
 * This class represents a token that does not follow the compact format of a JWT token.
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
 *
 */
class OpaqueToken(val value: String) : Token