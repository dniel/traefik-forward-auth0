package dniel.forwardauth.domain.authorize.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import dniel.forwardauth.ObjectMother
import dniel.forwardauth.domain.shared.InvalidToken
import dniel.forwardauth.domain.shared.JwtToken
import dniel.forwardauth.domain.shared.service.JwtDecoder
import dniel.forwardauth.domain.shared.service.VerifyTokenService
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

import static java.time.temporal.ChronoUnit.*
import static java.time.temporal.TemporalUnit.*
import static java.time.temporal.TemporalUnit.*
import static java.time.temporal.TemporalUnit.*
import static org.hamcrest.Matchers.*
import static spock.util.matcher.HamcrestSupport.that

class VerifyTokenServiceTest extends Specification {

    /**
     * {"typ":"JWT","alg":"HS256"}{"iss":"Online JWT Builder","iat":1550503393,"exp":1582039393,"aud":"www.example.com","sub":"daniel@example.com","email":"jrocket@example.com"}
     *
     */
    def "should accept valid token"() {
        given: "a valid JWT token with example values"
        def exampleAudience = ObjectMother.exampleAudience
        def expires = Date.from(ZonedDateTime.now().plus(1, HOURS).toInstant())

        def Algorithm algorithm = Algorithm.HMAC256("secret");
        def String token = JWT.create()
                .withIssuer("unittest")
                .withAudience("www.example.com")
                .withSubject("daniel@example.com")
                .withClaim("email", "daniel@example.com")
                .withExpiresAt(expires)
                .sign(algorithm)

        def exampleToken = JWT.decode(token)

        and: "a stubbed jwt decoder"
        def decoder = Stub(JwtDecoder) {
            verify(_) >> exampleToken
        }

        and: "a verification token service which is the system under test"
        VerifyTokenService sut = new VerifyTokenService(decoder)

        when: "we verify the token"
        def verifiedToken = sut.verify(token, exampleAudience)

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
