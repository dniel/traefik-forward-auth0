package dniel.forwardauth.domain.service

import com.auth0.jwt.exceptions.JWTVerificationException
import dniel.forwardauth.ObjectMother
import dniel.forwardauth.domain.InvalidToken
import dniel.forwardauth.domain.JwtToken
import spock.lang.Specification

import static org.hamcrest.Matchers.*
import static spock.util.matcher.HamcrestSupport.that

class VerifyTokenServiceTest extends Specification {

    def "should accept valid token"() {
        given: "a valid JWT token with example values"
        def domain = ObjectMother.domain
        def exampleAudience = ObjectMother.exampleAudience
        def exampleToken = ObjectMother.jwtToken
        def tokenString = ObjectMother.validJwtTokenString

        and: "a stubbed jwt decoder"
        def decoder = Stub(JwtDecoder) {
            verify(_) >> exampleToken
        }

        and: "a verification token service which is the system under test"
        VerifyTokenService sut = new VerifyTokenService(decoder)

        when: "we verify the token"
        def verifiedToken = sut.verify(tokenString, exampleAudience)

        then:
        that(verifiedToken, is(instanceOf(JwtToken)))
    }

    def "should fail if invalid audience in token"() {
        setup: "a JWT token with invalid audience"
        def domain = ObjectMother.domain
        def exampleAudience = "INVALID"
        def exampleToken = ObjectMother.jwtToken
        def tokenString = ObjectMother.validJwtTokenString

        and: "a stubbed jwt decoder"
        def decoder = Stub(JwtDecoder) {
            verify(_) >> exampleToken
        }

        and: "a verification token service which is the system under test"
        VerifyTokenService sut = new VerifyTokenService(decoder)

        when: "we verify the token"
        def token = sut.verify(tokenString, exampleAudience)

        then:
        that(token, is(instanceOf(InvalidToken)))
    }

    def "should return invalid token with reason if token fails to decode"() {
        setup: "a JWT token with invalid audience"
        def domain = ObjectMother.domain
        def exampleAudience = "INVALID"
        def exampleToken = ObjectMother.jwtToken
        def tokenString = ObjectMother.validJwtTokenString

        and: "a stubbed jwt decoder that throws an exception on verify"
        def decoder = Stub(JwtDecoder) {
            verify(_) >> {
                throw new JWTVerificationException("something went wrong.")
            }
        }

        and: "a verification token service which is the system under test"
        VerifyTokenService sut = new VerifyTokenService(decoder)

        when: "we verify the token"
        def token = sut.verify(tokenString, exampleAudience)

        then:
        that(token, is(instanceOf(InvalidToken)))
        that(token.reason, containsString("something went wrong."))
    }
}
