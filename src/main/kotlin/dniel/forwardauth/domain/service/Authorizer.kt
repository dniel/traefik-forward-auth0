package dniel.forwardauth.domain.service

import dniel.forwardauth.AuthProperties.Application
import dniel.forwardauth.domain.*
import org.slf4j.LoggerFactory

class Authorizer(accessToken: Token, idToken: Token, app: Application, nonce: Nonce,
                 originUrl: OriginUrl, state: State, authUrl: AuthorizeUrl, authDomain: String) : AuthorizerStateMachine.Delegate {

    private var fsm: AuthorizerStateMachine
    private var lastError: Error? = null

    companion object Factory {
        val LOGGER = LoggerFactory.getLogger(this::class.java)

        fun create(accessToken: Token, idToken: Token, app: Application, nonce: Nonce,
                   originUrl: OriginUrl, state: State, authUrl: AuthorizeUrl, authDomain: String):
                Authorizer = Authorizer(accessToken, idToken, app, nonce, originUrl, state, authUrl, authDomain)
    }

    init {
        fsm = AuthorizerStateMachine(this)
    }

    data class Error(val message: String)

    override val hasError: Boolean
        get() = lastError != null

    override fun onStartAuthorizing() {
        trace("onStartAuthorizing")
        fsm.post(AuthorizerStateMachine.Event.VALIDATE_REQUESTED_URL)
    }

    override fun onValidateProtectedUrl() {
        trace("onValidateProtectedUrl")
        fsm.post(AuthorizerStateMachine.Event.VALIDATE_WHITELISTED_URL)
    }

    override fun onValidateWhitelistedUrl() {
        trace("onValidateWhitelistedUrl")
        fsm.post(AuthorizerStateMachine.Event.RESTRICTED_URL)
    }

    override fun onValidateRestrictedMethod() {
        trace("onValidateRestrictedMethod")
        fsm.post(AuthorizerStateMachine.Event.RESTRICTED_METHOD)
    }

    override fun onStartValidateTokens() {
        trace("onStartValidateTokens")
        fsm.post(AuthorizerStateMachine.Event.VALIDATE_ACCESS_TOKEN)
    }

    override fun onValidateAccessToken() {
        trace("onValidateAccessToken")
        fsm.post(AuthorizerStateMachine.Event.VALID_ACCESS_TOKEN)
    }

    override fun onValidateIdToken() {
        trace("onValidateIdToken")
        fsm.post(AuthorizerStateMachine.Event.VALID_ID_TOKEN)
    }

    override fun onValidatePermissions() {
        trace("onValidatePermissions")
        fsm.post(AuthorizerStateMachine.Event.VALID_PERMISSIONS)
    }

    override fun onValidateSameSubs() {
        trace("onValidateSameSubs")
        fsm.post(AuthorizerStateMachine.Event.VALID_SAME_SUBS)
    }

    override fun onNeedRedirect() {
        trace("onNeedRedirect")
    }

    override fun onInvalidToken() {
        trace("onInvalidToken")
    }

    override fun onError() {
        trace("onError")
        trace(lastError!!.message)
    }

    override fun onAccessGranted() {
        trace("onAccessGranted")
    }

    override fun onAccessDenied() {
        trace("onAccessDenied")
    }

    /*
     */
    fun authorize(): AuthorizerStateMachine.State {
        return fsm.authorize()
    }

    fun trace(message: String) {
        LOGGER.debug(message)
    }

    fun state(): AuthorizerStateMachine.State {
        return fsm.state
    }
}

/*
fun main(args: Array<String>) {
    val app = Application()
    val aToken = InvalidToken("just for testing")
    val idToken = InvalidToken("just for testing")
    val nonce = Nonce.generate()
    val originUrl = OriginUrl("https", "www.exampple.com", "/", "GET")
    val state = State.create(originUrl, nonce)
    val authUrl = AuthorizeUrl("auth0autorhizeurl", app, state)
    val authDomain = "authdomain"

    val authorizer = Authorizer.create(aToken, idToken, app, nonce, originUrl, state, authUrl, authDomain)
    val output = authorizer.authorize()
    println(output)
}
 */