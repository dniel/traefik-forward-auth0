package dniel.forwardauth

import com.auth0.jwt.JWT
import dniel.forwardauth.domain.Anonymous
import dniel.forwardauth.infrastructure.micronaut.config.ApplicationSettings
import dniel.forwardauth.domain.Authenticated
import dniel.forwardauth.domain.JwtToken
import dniel.forwardauth.infrastructure.micronaut.config.ForwardAuthSettings

class ObjectMother {

    static def domain = "https://example.eu.auth0.com/"
    static def exampleAudience = "www.example.com"
    static def validJwtTokenString = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE1NTA1MDMzOTMsImV4cCI6MTU4MjAzOTM5MywiYXVkIjoid3d3LmV4YW1wbGUuY29tIiwic3ViIjoiZGFuaWVsQGV4YW1wbGUuY29tIiwiZW1haWwiOiJqcm9ja2V0QGV4YW1wbGUuY29tIn0.a_1TAIGDQBgt7nqLlSa9xsBD-gfl0-uPf2TQ5J1JyA8"
    static def jwtToken = JWT.decode(validJwtTokenString)
    static def validJwtToken = new JwtToken(jwtToken)
    static def userinfo = [sub: "daniel@example.com", email: "jrocket@example.com", uknown: "123"]
    static def authenticatedUser = new Authenticated(validJwtToken, validJwtToken, userinfo)
    static def anonymousUser = new Anonymous()
    static def properties

    static {
        // create the default app config
        def defaultApp = new ApplicationSettings("default")
        defaultApp.audience = exampleAudience
        defaultApp.name = "this is the default application"
        defaultApp.redirectUri = "https://www.example.test/oauth2/signin"
        defaultApp.tokenCookieDomain = "example.com"
        defaultApp.claims = ["sub", "email"]
        defaultApp.clientId = "123456789"
        defaultApp.clientSecret = "987654321"

        // and some more apps.
        def restricted = new ApplicationSettings("restricted.com")
        restricted.restrictedMethods = ["POST", "PUT", "DELETE", "PATCH"]

        // even more.
        def opaque = new ApplicationSettings("opaque.com")
        opaque.audience = "${domain}/userinfo"

        /**
         * Create new ForwardAuthSettings.
         * Use the test data created above to populate.
         */
        properties = new ForwardAuthSettings(defaultApp,  [defaultApp, restricted, opaque])
        properties.domain = domain
        properties.authorizeUrl = "${domain}authorize"
        properties.tokenEndpoint = "${domain}oauth/token"

    }

}
