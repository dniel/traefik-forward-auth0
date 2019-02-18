package dniel.forwardauth

import com.auth0.jwt.JWT

class ObjectMother {

    static def domain = "https://example.eu.auth0.com/"
    static def exampleAudience = "www.example.com"
    static def tokenString = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE1NTA1MDMzOTMsImV4cCI6MTU4MjAzOTM5MywiYXVkIjoid3d3LmV4YW1wbGUuY29tIiwic3ViIjoiZGFuaWVsQGV4YW1wbGUuY29tIiwiZW1haWwiOiJqcm9ja2V0QGV4YW1wbGUuY29tIn0.a_1TAIGDQBgt7nqLlSa9xsBD-gfl0-uPf2TQ5J1JyA8"
    static def exampleToken = JWT.decode(tokenString)
    static def properties = new AuthProperties()

    static {
        properties.domain = domain
        properties.authorizeUrl = "https://${domain}/authorize"
        properties.tokenEndpoint =  "https://${domain}/oauth/token"
    }

}
