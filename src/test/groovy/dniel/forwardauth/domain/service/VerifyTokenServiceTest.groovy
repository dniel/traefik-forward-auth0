package dniel.forwardauth.domain.service

import com.auth0.jwt.JWT
import dniel.forwardauth.infrastructure.jwt.JwtDecoder
import spock.lang.Specification

import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.notNullValue
import static spock.util.matcher.HamcrestSupport.that

class VerifyTokenServiceTest extends Specification {

    def "should accept valid token"() {
        given: "a valid JWT token with example values"
        def domain = "exampledomain"
        def expectedAudience = "www.example.com"
        def tokenString = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE1NTA1MDMzOTMsImV4cCI6MTU4MjAzOTM5MywiYXVkIjoid3d3LmV4YW1wbGUuY29tIiwic3ViIjoiZGFuaWVsQGV4YW1wbGUuY29tIiwiZW1haWwiOiJqcm9ja2V0QGV4YW1wbGUuY29tIn0.a_1TAIGDQBgt7nqLlSa9xsBD-gfl0-uPf2TQ5J1JyA8"
        def exampleToken = JWT.decode(tokenString)

        and: "a stubbed jwt decoder"
        def decoder = Stub(JwtDecoder) {
            verify(_, _) >> exampleToken
        }

        and: "a verification token service which is the system under test"
        VerifyTokenService sut = new VerifyTokenService(decoder)

        when: "we verify the token"
        def verifiedToken = sut.verify(tokenString, expectedAudience, domain)

        then:
        that(verifiedToken, is(notNullValue()))
    }

    def "should fail if invalid audience in token"() {
        setup: "a JWT token with invalid audience"
        def domain = "exampledomain"
        def expectedAudience = "123456"
        def tokenString = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE1NTA1MDMzOTMsImV4cCI6MTU4MjAzOTM5MywiYXVkIjoid3d3LmV4YW1wbGUuY29tIiwic3ViIjoiZGFuaWVsQGV4YW1wbGUuY29tIiwiZW1haWwiOiJqcm9ja2V0QGV4YW1wbGUuY29tIn0.a_1TAIGDQBgt7nqLlSa9xsBD-gfl0-uPf2TQ5J1JyA8"
        def exampleToken = JWT.decode(tokenString)

        and: "a stubbed jwt decoder"
        def decoder = Stub(JwtDecoder) {
            verify(tokenString, domain) >> exampleToken
        }

        and: "a verification token service which is the system under test"
        VerifyTokenService sut = new VerifyTokenService(decoder)

        when: "we verify the token"
        def verifiedToken = sut.verify(tokenString, expectedAudience, domain)

        then:
        def e = thrown(IllegalStateException)
    }
}
