package dniel.forwardauth.application

import dniel.forwardauth.ObjectMother
import spock.lang.Specification
import spock.lang.Unroll

import static dniel.forwardauth.ObjectMother.getAnonymousUser
import static dniel.forwardauth.ObjectMother.getAuthenticatedUser
import static org.hamcrest.Matchers.*
import static spock.util.matcher.HamcrestSupport.that

class AuthorizeHandlerTest extends Specification {

    @Unroll
    def "should grant access to #host#uri based on input parameters"() {
        given: "an authorize command with input parameters"
        def command = new AuthorizeHandler.AuthorizeCommand(
                user,
                protocol,
                host,
                uri,
                method,
                isApi)

        and: "a command handler that is the system under test"
        AuthorizeHandler sut = new AuthorizeHandler(ObjectMother.properties)

        when: "we authorize the request"
        def result = sut.handle(command)

        then: "we should get a valid response"
        that(result, is(instanceOf(AuthorizeHandler.AuthorizeEvent.AccessGranted)))

        where:
        user              | protocol | host               | uri              | method | isApi
        authenticatedUser | "HTTPS"  | "www.example.test" | "/test"          | "GET"  | false
        authenticatedUser | "HTTPS"  | "www.example.test" | "/oauth2/signin" | "GET"  | false
        authenticatedUser | "HTTPS"  | "www.example.test" | "/OaUth2/SiGNIn" | "GET"  | false
        authenticatedUser | "HTTPS"  | "opaque.com"       | "/test"          | "GET"  | false
        authenticatedUser | "HTTPS"  | "restricted.com"   | "/test"          | "GET"  | false
        authenticatedUser | "HTTPS"  | "restricted.com"   | "/test"          | "POST" | false
    }

    @Unroll
    def "should deny access to #host#uri based on input parameters"() {
        given: "an authorize command with input parameters"
        def command = new AuthorizeHandler.AuthorizeCommand(
                user,
                protocol,
                host,
                uri,
                method,
                isApi)

        and: "a command handler that is the system under test"
        AuthorizeHandler sut = new AuthorizeHandler(ObjectMother.properties)

        when: "we authorize the request"
        def result = sut.handle(command)

        then: "we should get a valid response"
        that(result, is(instanceOf(AuthorizeHandler.AuthorizeEvent.NeedRedirect)))

        where:
        user          | protocol | host               | uri     | method | isApi
        anonymousUser | "HTTPS"  | "www.example.test" | "/test" | "GET"  | false
        anonymousUser | "HTTPS"  | "www.example.test" | "/test" | "GeT"  | false
        anonymousUser | "HTTPS"  | "www.example.test" | "/test" | "GeT"  | false
        anonymousUser | "hTTpS"  | "WwW.ExaMplE.TeST" | "/test" | "GeT"  | false
        anonymousUser | "HTTPS"  | "www.example.test" | "/test" | "GET"  | false
        anonymousUser | "HTTPS"  | "www.example.test" | "/test" | "GeT"  | false
        anonymousUser | "HTTPS"  | "www.example.test" | "/test" | "GeT"  | false
        anonymousUser | "hTTpS"  | "WwW.ExaMplE.TeST" | "/test" | "GeT"  | false
    }

    @Unroll
    def "should handle errors when JWT token is not valid"() {
        given: "an authorize command with input parameters"
        def command = new AuthorizeHandler.AuthorizeCommand(
                user,
                protocol,
                host,
                uri,
                method,
                isApi)

        and: "a command handler that is the system under test"
        AuthorizeHandler sut = new AuthorizeHandler(ObjectMother.properties)

        when: "we authorize the request"
        def result = sut.handle(command)

        then: "we should get a valid response"
        that(result, is(instanceOf(AuthorizeHandler.AuthorizeEvent.NeedRedirect)))

        where:
        user          | protocol | host               | uri     | method | authenticated | restricted | isApi
        anonymousUser | "HTTPS"  | "www.example.test" | "/test" | "GET" || false         | true       | false
    }

    @Unroll
    def "should parse claim #key from idtoken"() {
        given: "an authorize command with input parameters"
        def command = new AuthorizeHandler.AuthorizeCommand(
                user,
                protocol,
                host,
                uri,
                method,
                isApi)

        and: "a command handler that is the system under test"
        AuthorizeHandler sut = new AuthorizeHandler(ObjectMother.properties)

        when: "we authorize the request"
        def result = sut.handle(command)

        then: "we should get a valid response"
        that(result.userinfo, hasEntry(key, value))

        where:
        user              | protocol | host               | uri     | method | isApi  | key     | value
        authenticatedUser | "HTTPS"  | "www.example.test" | "/test" | "GET"  | false || "sub"   | "daniel@example.com"
        authenticatedUser | "HTTPS"  | "www.example.test" | "/test" | "GET"  | false || "email" | "jrocket@example.com"
    }

    def "should ignore unknown claim from idtoken"() {
        given: "an authorize command with input parameters"
        def command = new AuthorizeHandler.AuthorizeCommand(
                user,
                protocol,
                host,
                uri,
                method,
                isAPi)

        and: "a command handler that is the system under test"
        AuthorizeHandler sut = new AuthorizeHandler(ObjectMother.properties)

        when: "we authorize the request"
        def result = sut.handle(command)

        then: "we should get a valid response"
        that(result.userinfo, not(hasKey(key)))

        where:
        user              | protocol | host               | uri     | method | isAPi  | key
        authenticatedUser | "HTTPS"  | "www.example.test" | "/test" | "GET"  | false || "a claim that is not in the jwt token"
    }

    def "should have authorization url for redirect url as configured in properties"() {
        given: "an authorize command with input parameters"
        def command = new AuthorizeHandler.AuthorizeCommand(
                anonymousUser,
                "https",
                "www.example.test",
                "/test",
                "GET",
                false)

        and: "a command handler that is the system under test"
        AuthorizeHandler sut = new AuthorizeHandler(ObjectMother.properties)

        when: "we authorize the request"
        def result = sut.handle(command)

        then: "we should get a valid response"
        that(result, is(instanceOf(AuthorizeHandler.AuthorizeEvent.NeedRedirect)))
        that(result.authorizeUrl.toString(), startsWith("https://example.eu.auth0.com/authorize"))
    }

    def "should redirect to authentication for non-api calls when anonymous"() {
        given: "an authorize command with input parameters"
        def command = new AuthorizeHandler.AuthorizeCommand(
                anonymousUser,
                "https",
                "www.example.test",
                "/test",
                "GET",
                false)

        and: "a command handler that is the system under test"
        AuthorizeHandler sut = new AuthorizeHandler(ObjectMother.properties)

        when: "we authorize the request"
        def result = sut.handle(command)

        then: "we should get a valid response"
        that(result, is(instanceOf(AuthorizeHandler.AuthorizeEvent.NeedRedirect)))
    }

    def "should deny access for api calls when anonymous user"() {
        given: "an authorize command with input parameters"
        def command = new AuthorizeHandler.AuthorizeCommand(
                anonymousUser,
                "https",
                "www.example.test",
                "/test",
                "GET",
                true)


        and: "a command handler that is the system under test"
        AuthorizeHandler sut = new AuthorizeHandler(ObjectMother.properties)

        when: "we authorize the request"
        def result = sut.handle(command)

        then: "we should get a valid response"
        that(result, is(instanceOf(AuthorizeHandler.AuthorizeEvent.AccessDenied)))
    }

    def "should have nonce when redirect to authenticate anonymous user"() {
        given: "an authorize command with input parameters"
        def command = new AuthorizeHandler.AuthorizeCommand(
                anonymousUser,
                "https",
                "www.example.test",
                "/test",
                "get",
                false)

        and: "a command handler that is the system under test"
        AuthorizeHandler sut = new AuthorizeHandler(ObjectMother.properties)

        when: "we authorize the request"
        def result = sut.handle(command)

        then: "we should get a valid response"
        that(result, is(instanceOf(AuthorizeHandler.AuthorizeEvent.NeedRedirect)))
        that(result.nonce, not(isEmptyOrNullString()))
    }
}