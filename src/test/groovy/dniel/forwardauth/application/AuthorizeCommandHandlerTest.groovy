package dniel.forwardauth.application

import dniel.forwardauth.ObjectMother
import dniel.forwardauth.domain.Token
import dniel.forwardauth.domain.service.NonceGeneratorService
import dniel.forwardauth.domain.service.VerifyTokenService
import spock.lang.Specification
import spock.lang.Unroll

import static dniel.forwardauth.ObjectMother.getJwtToken
import static dniel.forwardauth.ObjectMother.getValidJwtTokenString
import static org.hamcrest.Matchers.*
import static spock.util.matcher.HamcrestSupport.that

class AuthorizeCommandHandlerTest extends Specification {

    @Unroll
    def "should verify access to #host#uri based on input parameters"() {
        given: "an authorize command with input parameters"
        def command = new AuthorizeCommandHandler.AuthorizeCommand(
                jwt,
                jwt,
                protocol,
                host,
                uri,
                method)


        and: "a stub VerifyTokenService that return a valid JWT Token"
        def verifyTokenService = Stub(VerifyTokenService)
        verifyTokenService.verify(
                _,
                _,
                _) >> new Token(jwtToken)

        and: "a command handler that is the system under test"
        AuthorizeCommandHandler sut = new AuthorizeCommandHandler(
                ObjectMother.properties, verifyTokenService, new NonceGeneratorService())

        when: "we authorize the request"
        def result = sut.perform(command)

        then: "we should get a valid response"
        that(result.authenticated, is(authenticated))
        that(result.isRestrictedUrl, is(restricted))

        where:
        jwt                 | protocol | host               | uri              | method  | authenticated | restricted
        validJwtTokenString | "HTTPS"  | "www.example.test" | "/test"          | "GET"  || true          | true
        validJwtTokenString | "HTTPS"  | "www.example.test" | "/oauth2/signin" | "GET"  || true          | false
        validJwtTokenString | "HTTPS"  | "www.example.test" | "/OaUth2/SiGNIn" | "GET"  || true          | false
        validJwtTokenString | "HTTPS"  | "opaque.com"       | "/test"          | "GET"  || true          | true
        validJwtTokenString | "HTTPS"  | "restricted.com"   | "/test"          | "GET"  || true          | false
        validJwtTokenString | "HTTPS"  | "restricted.com"   | "/test"          | "POST" || true          | true
        null                | "HTTPS"  | "www.example.test" | "/test"          | "GET"  || false         | true
        null                | "HTTPS"  | "www.example.test" | "/test"          | "GeT"  || false         | true
        null                | "HTTPS"  | "www.example.test" | "/test"          | "GeT"  || false         | true
        null                | "hTTpS"  | "WwW.ExaMplE.TeST" | "/test"          | "GeT"  || false         | true
    }


    @Unroll
    def "should handle errors when JWT token is not valid"() {
        given: "an authorize command with input parameters"
        def command = new AuthorizeCommandHandler.AuthorizeCommand(
                jwt,
                jwt,
                protocol,
                host,
                uri,
                method)


        and: "a stub VerifyTokenService that return a valid JWT Token"
        def verifyTokenService = Stub(VerifyTokenService)
        verifyTokenService.verify(
                _,
                _,
                _) >> { throw new IllegalStateException("Simulate a verification error") }

        and: "a command handler that is the system under test"
        AuthorizeCommandHandler sut = new AuthorizeCommandHandler(
                ObjectMother.properties, verifyTokenService, new NonceGeneratorService())

        when: "we authorize the request"
        def result = sut.perform(command)

        then: "we should get a valid response"
        that(result.authenticated, is(authenticated))
        that(result.isRestrictedUrl, is(restricted))

        where:
        jwt                  | protocol | host               | uri     | method | authenticated | restricted
        "invalid jwt string" | "HTTPS"  | "www.example.test" | "/test" | "GET" || false         | true
    }

    @Unroll
    def "should handle when only one of id-token or access-token is present"() {
        given: "an authorize command with input parameters"
        def command = new AuthorizeCommandHandler.AuthorizeCommand(
                accesstoken,
                idtoken,
                protocol,
                host,
                uri,
                method)


        and: "a stub VerifyTokenService that return a valid JWT Token"
        def verifyTokenService = Stub(VerifyTokenService)
        verifyTokenService.verify(
                _,
                _,
                _) >> new Token(jwtToken)

        and: "a command handler that is the system under test"
        AuthorizeCommandHandler sut = new AuthorizeCommandHandler(
                ObjectMother.properties, verifyTokenService, new NonceGeneratorService())

        when: "we authorize the request"
        def result = sut.perform(command)

        then: "we should get a valid response"
        that(result.authenticated, is(authenticated))
        that(result.isRestrictedUrl, is(restricted))

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
        def command = new AuthorizeCommandHandler.AuthorizeCommand(
                jwt,
                jwt,
                protocol,
                host,
                uri,
                method)


        and: "a stub VerifyTokenService that return a valid JWT Token"
        def verifyTokenService = Stub(VerifyTokenService)
        verifyTokenService.verify(
                _,
                _,
                _) >> new Token(jwtToken)

        and: "a command handler that is the system under test"
        AuthorizeCommandHandler sut = new AuthorizeCommandHandler(
                ObjectMother.properties, verifyTokenService, new NonceGeneratorService())

        when: "we authorize the request"
        def result = sut.perform(command)

        then: "we should get a valid response"
        that(result.userinfo, hasEntry(key, value))

        where:
        jwt                 | protocol | host               | uri     | method | key     | value
        validJwtTokenString | "HTTPS"  | "www.example.test" | "/test" | "GET" || "sub"   | "daniel@example.com"
        validJwtTokenString | "HTTPS"  | "www.example.test" | "/test" | "GET" || "email" | "jrocket@example.com"
    }

    def "should ignore unknown claim from idtoken"() {
        given: "an authorize command with input parameters"
        def command = new AuthorizeCommandHandler.AuthorizeCommand(
                jwt,
                jwt,
                protocol,
                host,
                uri,
                method)


        and: "a stub VerifyTokenService that return a valid JWT Token"
        def verifyTokenService = Stub(VerifyTokenService)
        verifyTokenService.verify(
                _,
                _,
                _) >> new Token(jwtToken)

        and: "a command handler that is the system under test"
        AuthorizeCommandHandler sut = new AuthorizeCommandHandler(
                ObjectMother.properties, verifyTokenService, new NonceGeneratorService())

        when: "we authorize the request"
        def result = sut.perform(command)

        then: "we should get a valid response"
        that(result.userinfo, not(hasKey(key)))

        where:
        jwt                 | protocol | host               | uri     | method | key
        validJwtTokenString | "HTTPS"  | "www.example.test" | "/test" | "GET" || "a claim that is not in the jwt token"
    }

}