package dniel.forwardauth.domain.service

import dniel.forwardauth.AuthProperties.Application
import dniel.forwardauth.domain.*
import org.slf4j.LoggerFactory

class Authorizer private constructor(val accessToken: Token, val idToken: Token,
                                     val app: Application, val originUrl: RequestedUrl,
                                     override val isApi: Boolean) : AuthorizerStateMachine.Delegate {

    private var fsm: AuthorizerStateMachine

    companion object Factory {
        val LOGGER = LoggerFactory.getLogger(this::class.java)

        fun create(accessToken: Token, idToken: Token, app: Application,
                   originUrl: RequestedUrl, isApi: Boolean):
                Authorizer = Authorizer(accessToken, idToken, app, originUrl, isApi)
    }

    init {
        fsm = AuthorizerStateMachine(this)
    }

    data class Error(val message: String)

    var lastError: Error? = null
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
        fun isSigninUrl(originUrl: RequestedUrl, app: Application) =
                originUrl.startsWith(app.redirectUri)

        if (isSigninUrl(originUrl, app)) {
            fsm.post(AuthorizerStateMachine.Event.WHITELISTED_URL)
        } else {
            fsm.post(AuthorizerStateMachine.Event.RESTRICTED_URL)
        }
    }

    override fun onValidateRestrictedMethod() {
        trace("onValidateRestrictedMethod")
        val method = originUrl.method
        fun isRestrictedMethod(app: Application, method: String) =
                app.restrictedMethods.any() { t -> t.equals(method, true) }

        when {
            isRestrictedMethod(app, method) -> fsm.post(AuthorizerStateMachine.Event.RESTRICTED_METHOD)
            else -> fsm.post(AuthorizerStateMachine.Event.UNRESTRICTED_METHOD)
        }
    }

    override fun onStartValidateTokens() {
        trace("onStartValidateTokens")
        fsm.post(AuthorizerStateMachine.Event.VALIDATE_ACCESS_TOKEN)
    }

    override fun onValidateAccessToken() {
        trace("onValidateAccessToken")
        when {
            accessToken is OpaqueToken -> {
                lastError = Error("Opaque Access Tokens is not supported.")
                fsm.post(AuthorizerStateMachine.Event.ERROR)
            }
            accessToken is JwtToken -> fsm.post(AuthorizerStateMachine.Event.VALID_ACCESS_TOKEN)
            accessToken is InvalidToken -> fsm.post(AuthorizerStateMachine.Event.INVALID_ACCESS_TOKEN)
        }
    }

    override fun onValidateIdToken() {
        trace("onValidateIdToken")
        when {
            idToken is JwtToken -> fsm.post(AuthorizerStateMachine.Event.VALID_ID_TOKEN)
            else -> fsm.post(AuthorizerStateMachine.Event.INVALID_ID_TOKEN)
        }
    }

    override fun onValidatePermissions() {
        trace("onValidatePermissions")
        when {
            (accessToken as JwtToken).hasPermission(app.requiredPermissions) -> fsm.post(AuthorizerStateMachine.Event.VALID_PERMISSIONS)
            else -> {
                lastError = Error("Missing permission/s for user.")
                fsm.post(AuthorizerStateMachine.Event.INVALID_PERMISSIONS)
            }
        }
    }

    override fun onValidateSameSubs() {
        trace("onValidateSameSubs")
        fun hasSameSubs(accessToken: Token, idToken: Token) =
                accessToken is JwtToken && idToken is JwtToken && idToken.subject()  == accessToken.subject()

        // check if both tokens have the same subject
        if (hasSameSubs(accessToken, idToken)) {
            fsm.post(AuthorizerStateMachine.Event.VALID_SAME_SUBS)
        } else {
            lastError = Error("Access Token and Id Token had different value in SUB-claim.")
            fsm.post(AuthorizerStateMachine.Event.INVALID_SAME_SUBS)
        }
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
        LOGGER.trace(message)
    }

    fun state(): AuthorizerStateMachine.State {
        return fsm.state
    }
}
