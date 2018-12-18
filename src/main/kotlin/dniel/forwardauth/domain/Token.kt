package dniel.forwardauth.domain

import com.auth0.jwt.interfaces.DecodedJWT

class Token(val value: DecodedJWT) {
}