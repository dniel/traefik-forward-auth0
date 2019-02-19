package dniel.forwardauth.application

import dniel.forwardauth.ObjectMother
import dniel.forwardauth.domain.Token
import dniel.forwardauth.domain.service.NonceGeneratorService
import dniel.forwardauth.domain.service.VerifyTokenService
import spock.lang.Specification

import static org.hamcrest.Matchers.is
import static spock.util.matcher.HamcrestSupport.that

class AuthorizeCommandHandlerTest extends Specification {

    def "should accept valid tokens for a restricted url"() {
        given: "an authorizecommand with valid parameters"
        def command = new AuthorizeCommandHandler.AuthorizeCommand(
                ObjectMother.jwtTokenString,
                ObjectMother.jwtTokenString,
                "HTTPS",
                "www.example.test",
                "/bla/bla",
                "GET")

        and: "a stub verifytokenservice that return a valid JWT Token"
        def verifyTokenService = Stub(VerifyTokenService)
        verifyTokenService.verify(
                ObjectMother.jwtTokenString,
                _,
                ObjectMother.domain) >> new Token(ObjectMother.jwtToken)

        and: "a command handler that is the system under test"
        AuthorizeCommandHandler sut = new AuthorizeCommandHandler(
                ObjectMother.properties, verifyTokenService, new NonceGeneratorService())

        when: "we authorize the request"
        def result = sut.perform(command)

        then: "we should get a valid response"
        that(result.authenticated, is(true))
        that(result.isRestrictedUrl, is(true))
    }

    def "should accept a non-restricted method"() {
        given: "an authorizecommand with valid parameters"
        def command = new AuthorizeCommandHandler.AuthorizeCommand(
                ObjectMother.jwtTokenString,
                ObjectMother.jwtTokenString,
                "HTTPS",
                "restricted.com",
                "/bla/bla",
                "GET")


        and: "a stub VerifyTokenService that return a valid JWT Token"
        def verifyTokenService = Stub(VerifyTokenService)
        verifyTokenService.verify(
                ObjectMother.jwtTokenString,
                _,
                ObjectMother.domain) >> new Token(ObjectMother.jwtToken)

        and: "a command handler that is the system under test"
        AuthorizeCommandHandler sut = new AuthorizeCommandHandler(
                ObjectMother.properties, verifyTokenService, new NonceGeneratorService())

        when: "we authorize the request"
        def result = sut.perform(command)

        then: "we should get a valid response"
        that(result.authenticated, is(true))
        that(result.isRestrictedUrl, is(false))
    }

    def "should accept the redirect url as non-restricted"() {
        given: "an authorizecommand with redirect-url parameters"
        def command = new AuthorizeCommandHandler.AuthorizeCommand(
                ObjectMother.jwtTokenString,
                ObjectMother.jwtTokenString,
                "HTTPS",
                "www.example.test",
                "/oauth2/signin",
                "GET")


        and: "a stub VerifyTokenService that return a valid JWT Token"
        def verifyTokenService = Stub(VerifyTokenService)
        verifyTokenService.verify(
                ObjectMother.jwtTokenString,
                _,
                ObjectMother.domain) >> new Token(ObjectMother.jwtToken)

        and: "a command handler that is the system under test"
        AuthorizeCommandHandler sut = new AuthorizeCommandHandler(
                ObjectMother.properties, verifyTokenService, new NonceGeneratorService())

        when: "we authorize the request"
        def result = sut.perform(command)

        then: "we should get a valid response"
        that(result.authenticated, is(true))
        that(result.isRestrictedUrl, is(false))
    }

}
