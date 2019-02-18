package dniel.forwardauth.domain.service

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.ObjectMother
import dniel.forwardauth.application.AuthorizeCommandHandler
import dniel.forwardauth.domain.Token
import spock.lang.Specification

class AuthorizeCommandHandlerTest extends Specification {

    def "should authorize request"() {
        given:
        def AuthorizeCommandHandler.AuthorizeCommand command = new AuthorizeCommandHandler.AuthorizeCommand(
                "accesstoken",
                "idtoken",
                "https",
                "example.com",
                "/bla/bla",
                "GET")

        def AuthProperties properties = ObjectMother.properties


        and:
        def verifyTokenService = Stub(VerifyTokenService)
        verifyTokenService.verify(_, _, _) >> new Token(ObjectMother.exampleToken)

        and:
        AuthorizeCommandHandler sut = new AuthorizeCommandHandler(properties, verifyTokenService, new NonceGeneratorService())


        when:
        def result = sut.perform(command)
        println result

        then:
        result != null
    }

}
