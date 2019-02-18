package dniel.forwardauth.domain.service

import dniel.forwardauth.AuthProperties
import dniel.forwardauth.application.AuthorizeCommandHandler
import spock.lang.Specification

class AuthorizeCommandHandlerTest extends Specification {

    NonceGeneratorService nonceGenSvc = new NonceGeneratorService()
    VerifyTokenService verifyTokenService = new VerifyTokenService()
    AuthProperties properties = new AuthProperties()

    AuthorizeCommandHandler sut = new AuthorizeCommandHandler()

    void setup() {
        sut.nonceService = nonceGenSvc
        sut.verifyTokenService = verifyTokenService
        sut.properties = properties
    }

    void cleanup() {
    }

    def "should authorize request"() {
        given:
        def AuthorizeCommandHandler.AuthorizeCommand command = new AuthorizeCommandHandler.AuthorizeCommand()
        command.accessToken = "dsfdsf"
        command.host = "lskdlasdlkjsad"
        command.idToken = "kjsahdkjd"
        command.method = "sdfksjdf"
        command.protocol = "kjsdkjhfd"
        command.uri = "kjdshkjsdfkjshfd"

        when:
        def result = sut.perform(command)

        then:
        result != null

    }

}
