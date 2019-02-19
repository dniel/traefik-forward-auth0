package dniel.forwardauth.domain.service


import dniel.forwardauth.ObjectMother
import dniel.forwardauth.infrastructure.jwt.JwtDecoder
import spock.lang.Specification

import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.notNullValue
import static spock.util.matcher.HamcrestSupport.that

class VerifyTokenServiceTest extends Specification {

    def "should accept valid token"() {
        given: "a valid JWT token with example values"
        def domain = ObjectMother.domain
        def exampleAudience = ObjectMother.exampleAudience
        def exampleToken = ObjectMother.jwtToken
        def tokenString = ObjectMother.jwtTokenString

        and: "a stubbed jwt decoder"
        def decoder = Stub(JwtDecoder) {
            verify(_, _) >> exampleToken
        }

        and: "a verification token service which is the system under test"
        VerifyTokenService sut = new VerifyTokenService(decoder)

        when: "we verify the token"
        def verifiedToken = sut.verify(tokenString, exampleAudience, domain)

        then:
        that(verifiedToken, is(notNullValue()))
    }

    def "should fail if invalid audience in token"() {
        setup: "a JWT token with invalid audience"
        def domain = ObjectMother.domain
        def exampleAudience = "INVALID"
        def exampleToken = ObjectMother.jwtToken
        def tokenString = ObjectMother.jwtTokenString

        and: "a stubbed jwt decoder"
        def decoder = Stub(JwtDecoder) {
            verify(_, _) >> exampleToken
        }

        and: "a verification token service which is the system under test"
        VerifyTokenService sut = new VerifyTokenService(decoder)

        when: "we verify the token"
        sut.verify(tokenString, exampleAudience, domain)

        then:
        thrown(IllegalStateException)
    }
}
