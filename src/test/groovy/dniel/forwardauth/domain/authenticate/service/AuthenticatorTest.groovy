package dniel.forwardauth.domain.authenticate.service

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import static org.hamcrest.Matchers.equalTo
import static spock.util.matcher.HamcrestSupport.that

@MicronautTest
class AuthenticatorTest extends Specification {
    def "should test the authenticator"() {
        given: "some input"

        when: "we do something with the authenticator"
        def hello = "world"

        then: "we should verify the outcome"
        that(hello, equalTo("world"))
    }
}
