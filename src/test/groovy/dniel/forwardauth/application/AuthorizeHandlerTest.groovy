package dniel.forwardauth.application

import dniel.forwardauth.ObjectMother
import dniel.forwardauth.domain.InvalidToken
import dniel.forwardauth.domain.JwtToken
import dniel.forwardauth.domain.service.VerifyTokenService
import spock.lang.Specification
import spock.lang.Unroll

import static dniel.forwardauth.ObjectMother.getJwtToken
import static dniel.forwardauth.ObjectMother.getValidJwtTokenString
import static org.hamcrest.Matchers.*
import static spock.util.matcher.HamcrestSupport.that

class AuthorizeHandlerTest extends Specification {

    @Unroll
    def "should grant access to #host#uri based on input parameters"() {
        given: "an authorize command with input parameters"
        def command = new AuthorizeHandler.AuthorizeCommand(
                jwt,
                jwt,
                protocol,
                host,
                uri,
                method)


        and: "a stub VerifyTokenService that return a valid JWT JwtToken"
        def verifyTokenService = Stub(VerifyTokenService)
        verifyTokenService.verify(validJwtTokenString, _) >> new JwtToken(jwtToken)

        and: "a command handler that is the system under test"
        AuthorizeHandler sut = new AuthorizeHandler(
                ObjectMother.properties, verifyTokenService)

        when: "we authorize the request"
        def result = sut.handle(command)

        then: "we should get a valid response"
        that(result, is(instanceOf(AuthorizeHandler.AuthEvent.AccessGranted)))

        where:
        jwt                 | protocol | host               | uri              | method
        validJwtTokenString | "HTTPS"  | "www.example.test" | "/test"          | "GET"
        validJwtTokenString | "HTTPS"  | "www.example.test" | "/oauth2/signin" | "GET"
        validJwtTokenString | "HTTPS"  | "www.example.test" | "/OaUth2/SiGNIn" | "GET"
        validJwtTokenString | "HTTPS"  | "opaque.com"       | "/test"          | "GET"
        validJwtTokenString | "HTTPS"  | "restricted.com"   | "/test"          | "GET"
        validJwtTokenString | "HTTPS"  | "restricted.com"   | "/test"          | "POST"
    }

    @Unroll
    def "should deny access to #host#uri based on input parameters"() {
        given: "an authorize command with input parameters"
        def command = new AuthorizeHandler.AuthorizeCommand(
                jwt,
                jwt,
                protocol,
                host,
                uri,
                method)


        and: "a stub VerifyTokenService that return a valid JWT JwtToken"
        def verifyTokenService = Stub(VerifyTokenService)
        verifyTokenService.verify(null, _) >> new InvalidToken("missing token return invalid token.")
        verifyTokenService.verify("", _) >> new InvalidToken("missing token return invalid token.")

        and: "a command handler that is the system under test"
        AuthorizeHandler sut = new AuthorizeHandler(
                ObjectMother.properties, verifyTokenService)

        when: "we authorize the request"
        def result = sut.handle(command)

        then: "we should get a valid response"
        that(result, is(instanceOf(AuthorizeHandler.AuthEvent.NeedRedirect)))

        where:
        jwt  | protocol | host               | uri     | method
        null | "HTTPS"  | "www.example.test" | "/test" | "GET"
        null | "HTTPS"  | "www.example.test" | "/test" | "GeT"
        null | "HTTPS"  | "www.example.test" | "/test" | "GeT"
        null | "hTTpS"  | "WwW.ExaMplE.TeST" | "/test" | "GeT"
        ""   | "HTTPS"  | "www.example.test" | "/test" | "GET"
        ""   | "HTTPS"  | "www.example.test" | "/test" | "GeT"
        ""   | "HTTPS"  | "www.example.test" | "/test" | "GeT"
        ""   | "hTTpS"  | "WwW.ExaMplE.TeST" | "/test" | "GeT"
    }

    @Unroll
    def "should handle errors when JWT token is not valid"() {
        given: "an authorize command with input parameters"
        def command = new AuthorizeHandler.AuthorizeCommand(
                jwt,
                jwt,
                protocol,
                host,
                uri,
                method)


        and: "a stub VerifyTokenService that return a valid JWT JwtToken"
        def verifyTokenService = Stub(VerifyTokenService)
        verifyTokenService.verify( _, _) >> new InvalidToken("simulating an invalid token resposne from the token service.")

        and: "a command handler that is the system under test"
        AuthorizeHandler sut = new AuthorizeHandler(
                ObjectMother.properties, verifyTokenService)

        when: "we authorize the request"
        def result = sut.handle(command)

        then: "we should get a valid response"
        that(result, is(instanceOf(AuthorizeHandler.AuthEvent.NeedRedirect)))

        where:
        jwt                  | protocol | host               | uri     | method | authenticated | restricted
        "invalid jwt string" | "HTTPS"  | "www.example.test" | "/test" | "GET" || false         | true
    }

    @Unroll
    def "should handle when only one of id-token or access-token is present"() {
        given: "an authorize command with input parameters"
        def command = new AuthorizeHandler.AuthorizeCommand(
                accesstoken,
                idtoken,
                protocol,
                host,
                uri,
                method)


        and: "a stub VerifyTokenService that return a valid JWT JwtToken"
        def verifyTokenService = Stub(VerifyTokenService)
        verifyTokenService.verify(validJwtTokenString, _) >> new JwtToken(jwtToken)
        verifyTokenService.verify(null, _) >> new InvalidToken("missing token return invalid token.")
        verifyTokenService.verify("", _) >> new InvalidToken("missing token return invalid token.")

        and: "a command handler that is the system under test"
        AuthorizeHandler sut = new AuthorizeHandler(
                ObjectMother.properties, verifyTokenService)

        when: "we authorize the request"
        def result = sut.handle(command)

        then: "we should get a valid response"
        that(result, is(instanceOf(AuthorizeHandler.AuthEvent.NeedRedirect)))

        where:
        idtoken             | accesstoken         | protocol | host               | uri     | method | authenticated | restricted
        validJwtTokenString | null                | "HTTPS"  | "www.example.test" | "/test" | "GET" || false         | true
        validJwtTokenString | ""                  | "HTTPS"  | "www.example.test" | "/test" | "GET" || false         | true
        null                | validJwtTokenString | "HTTPS"  | "www.example.test" | "/test" | "GET" || false         | true
        ""                  | validJwtTokenString | "HTTPS"  | "www.example.test" | "/test" | "GET" || false         | true
        ""                  | null                | "HTTPS"  | "www.example.test" | "/test" | "GET" || false         | true
        null                | null                | "HTTPS"  | "www.example.test" | "/test" | "GET" || false         | true
        ""                  | ""                  | "HTTPS"  | "www.example.test" | "/test" | "GET" || false         | true
    }

    @Unroll
    def "should parse claim #key from idtoken"() {
        given: "an authorize command with input parameters"
        def command = new AuthorizeHandler.AuthorizeCommand(
                jwt,
                jwt,
                protocol,
                host,
                uri,
                method)


        and: "a stub VerifyTokenService that return a valid JWT JwtToken"
        def verifyTokenService = Stub(VerifyTokenService)
        verifyTokenService.verify(_,_) >> new JwtToken(jwtToken)

        and: "a command handler that is the system under test"
        AuthorizeHandler sut = new AuthorizeHandler(
                ObjectMother.properties, verifyTokenService)

        when: "we authorize the request"
        def result = sut.handle(command)

        then: "we should get a valid response"
        that(result.userinfo, hasEntry(key, value))

        where:
        jwt                 | protocol | host               | uri     | method | key     | value
        validJwtTokenString | "HTTPS"  | "www.example.test" | "/test" | "GET" || "sub"   | "daniel@example.com"
        validJwtTokenString | "HTTPS"  | "www.example.test" | "/test" | "GET" || "email" | "jrocket@example.com"
    }

    def "should ignore unknown claim from idtoken"() {
        given: "an authorize command with input parameters"
        def command = new AuthorizeHandler.AuthorizeCommand(
                jwt,
                jwt,
                protocol,
                host,
                uri,
                method)


        and: "a stub VerifyTokenService that return a valid JWT JwtToken"
        def verifyTokenService = Stub(VerifyTokenService)
        verifyTokenService.verify( _, _) >> new JwtToken(jwtToken)

        and: "a command handler that is the system under test"
        AuthorizeHandler sut = new AuthorizeHandler(
                ObjectMother.properties, verifyTokenService)

        when: "we authorize the request"
        def result = sut.handle(command)

        then: "we should get a valid response"
        that(result.userinfo, not(hasKey(key)))

        where:
        jwt                 | protocol | host               | uri     | method | key
        validJwtTokenString | "HTTPS"  | "www.example.test" | "/test" | "GET" || "a claim that is not in the jwt token"
    }

    def "should have authorization url for redirect url as configured in properties"() {
        given: "an authorize command with input parameters"
        def command = new AuthorizeHandler.AuthorizeCommand(
                validJwtTokenString,
                validJwtTokenString,
                "https",
                "www.example.test",
                "/test",
                "GET")


        and: "a stub VerifyTokenService that return a valid JWT JwtToken"
        def verifyTokenService = Stub(VerifyTokenService)
        verifyTokenService.verify(_, _) >> new InvalidToken(("Just to get a redirect event to check"))

        and: "a command handler that is the system under test"
        AuthorizeHandler sut = new AuthorizeHandler(
                ObjectMother.properties, verifyTokenService)

        when: "we authorize the request"
        def result = sut.handle(command)

        then: "we should get a valid response"
        that(result, is(instanceOf(AuthorizeHandler.AuthEvent.NeedRedirect)))
        that(result.authorizeUrl.toString(), startsWith("https://example.eu.auth0.com/authorize"))
    }

    def "should have nonce set in result"() {
        given: "an authorize command with input parameters"
        def command = new AuthorizeHandler.AuthorizeCommand(
                validJwtTokenString,
                validJwtTokenString,
                "https",
                "www.example.test",
                "/test",
                "get")


        and: "a stub VerifyTokenService that return a valid JWT JwtToken"
        def verifyTokenService = Stub(VerifyTokenService)
        verifyTokenService.verify(_, _) >> new InvalidToken(("Just to get a redirect event to check"))

        and: "a command handler that is the system under test"
        AuthorizeHandler sut = new AuthorizeHandler(
                ObjectMother.properties, verifyTokenService)

        when: "we authorize the request"
        def result = sut.handle(command)

        then: "we should get a valid response"
        that(result, is(instanceOf(AuthorizeHandler.AuthEvent.NeedRedirect)))
        that(result.nonce, not(isEmptyOrNullString()))
    }
}