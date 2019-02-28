package dniel.forwardauth.domain.service

import spock.lang.Specification

import static org.hamcrest.Matchers.*
import static spock.util.matcher.HamcrestSupport.that

class NonceGeneratorServiceTest extends Specification {

    NonceGeneratorService sut = new NonceGeneratorService()

    def "should generate a nonce"() {
        given:
        def nonce1 = sut.generate()

        expect:
        that(nonce1, is(notNullValue()))
    }

    def "should generate different nonce each time"() {
        given:
        def nonce1 = sut.generate()
        def nonce2 = sut.generate()

        expect:
        that(nonce1, is(not(nonce2)))
    }
}
