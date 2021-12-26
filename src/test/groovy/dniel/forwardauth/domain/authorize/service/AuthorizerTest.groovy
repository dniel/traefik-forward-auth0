package dniel.forwardauth.domain.authorize.service

import spock.lang.Specification

import static org.hamcrest.Matchers.equalTo
import static spock.util.matcher.HamcrestSupport.that

class AuthorizerTest extends Specification {
    def "should test the authorizer"() {
        given: "some input"

        when: "we do something with the authorizer"
        def hello = "world"

        then: "we should verify the outcome"
        that(hello, equalTo("world"))
    }
}
