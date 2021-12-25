package dniel.forwardauth

import com.auth0.jwt.JWT
import dniel.forwardauth.domain.Anonymous
import dniel.forwardauth.domain.Application
import dniel.forwardauth.domain.Authenticated
import dniel.forwardauth.domain.JwtToken
import dniel.forwardauth.infrastructure.micronaut.config.AuthProperties

class ObjectMother {

    static def domain = "https://example.eu.auth0.com/"
    static def exampleAudience = "www.example.com"
    static def validJwtTokenString = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE1NTA1MDMzOTMsImV4cCI6MTU4MjAzOTM5MywiYXVkIjoid3d3LmV4YW1wbGUuY29tIiwic3ViIjoiZGFuaWVsQGV4YW1wbGUuY29tIiwiZW1haWwiOiJqcm9ja2V0QGV4YW1wbGUuY29tIn0.a_1TAIGDQBgt7nqLlSa9xsBD-gfl0-uPf2TQ5J1JyA8"
    static def jwtToken = JWT.decode(validJwtTokenString)
    static def validJwtToken = new JwtToken(jwtToken)
    static def userinfo = [sub: "daniel@example.com", email: "jrocket@example.com", uknown: "123"]
    static def authenticatedUser = new Authenticated(validJwtToken, validJwtToken, userinfo)
    static def anonymousUser = new Anonymous()
    static def properties = new AuthProperties()

    static {
        properties.domain = domain
        properties.authorizeUrl = "${domain}authorize"
        properties.tokenEndpoint = "${domain}oauth/token"

        properties.default.audience = exampleAudience
        properties.default.name = "this is the default application"
        properties.default.redirectUri = "https://www.example.test/oauth2/signin"
        properties.default.tokenCookieDomain = "example.com"
        properties.default.claims = ["sub", "email"]
        properties.default.clientId = "123456789"
        properties.default.clientSecret = "987654321"

        properties.apps << new Application()
        properties.apps[0].name = "restricted.com"
        properties.apps[0].restrictedMethods = ["POST", "PUT", "DELETE", "PATCH"]

        properties.apps << new Application()
        properties.apps[1].name = "opaque.com"
        properties.apps[1].audience = "${domain}/userinfo"
    }

}
