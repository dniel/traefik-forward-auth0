package dniel.forwardauth.domain.service

import com.github.oxo42.stateless4j.StateMachine
import com.github.oxo42.stateless4j.StateMachineConfig
import java.util.*

class AuthorizerStateMachine(initialState: State, private val delegate: Delegate) {

    constructor(delegate: Delegate) : this(State.AWAIT_AUTHORIZING, delegate)

    interface Delegate {
        val hasError: Boolean

        fun onStartAuthorizing()
        fun onStartValidateTokens()
        fun onError()
        fun onValidateAccessToken()
        fun onValidateIdToken()
        fun onValidatePermissions()
        fun onValidateSameSubs()
        fun onValidateProtectedUrl()
        fun onValidateWhitelistedUrl()
        fun onValidateRestrictedMethod()
        fun onAccessGranted()
        fun onAccessDenied()
        fun onNeedRedirect()
        fun onInvalidToken()
    }

    enum class State {
        AWAIT_AUTHORIZING,
        AUTHORIZING,

        VALIDATING_TOKENS,

        VALIDATING_ID_TOKEN,
        VALIDATING_ACCESS_TOKEN,
        VALIDATING_PERMISSIONS,
        VALIDATING_SAME_SUBS,

        VALIDATING_REQUESTED_URL,
        VALIDATING_WHITELISTED_URL,
        VALIDATING_RESTRICTED_METHOD,

        ACCESS_GRANTED,
        ACCESS_DENIED,
        NEED_REDIRECT,
        INVALID_TOKEN,
        ERROR,

    }

    enum class Event {
        AUTHORIZE,

        VALIDATE_REQUESTED_URL,
        VALIDATE_WHITELISTED_URL,
        VALIDATE_RESTRICTED_METHOD,

        WHITELISTED_URL,
        RESTRICTED_URL,
        RESTRICTED_METHOD,
        UNRESTRICTED_METHOD,

        // verify access token and its content
        VALIDATE_ACCESS_TOKEN,
        VALID_ACCESS_TOKEN,
        INVALID_ACCESS_TOKEN,
        VALID_PERMISSIONS,
        INVALID_PERMISSIONS,
        INVALID_SAME_SUBS,
        VALID_SAME_SUBS,

        // verify ID token and its content
        VALIDATE_ID_TOKEN,
        VALID_ID_TOKEN,
        INVALID_ID_TOKEN,

        // generic error event
        ERROR,
        IMMEDIATE_TRANSITION
    }


    private var pendingEvents = ArrayDeque<Event>()
    private var isProcessing = false
    private val fsm: StateMachine<State, Event>

    /**
     * Immediate state machine state. This attribute provides innermost active state.
     * For checking parent states, use [PlayerStateMachine.isInState].
     */
    val state: State
        get() {
            return fsm.state
        }

    init {
        val config = StateMachineConfig<State, Event>()

        config.configure(State.AWAIT_AUTHORIZING)
                .permit(Event.AUTHORIZE, State.AUTHORIZING)

        config.configure(State.AUTHORIZING)
                .permit(Event.VALIDATE_REQUESTED_URL, State.VALIDATING_REQUESTED_URL)
                .permitIf(Event.ERROR, State.ERROR) { delegate.hasError }
                .onEntry(delegate::onStartAuthorizing)

        config.configure(State.VALIDATING_REQUESTED_URL)
                .substateOf(State.AUTHORIZING)
                .permit(Event.VALIDATE_WHITELISTED_URL, State.VALIDATING_WHITELISTED_URL)
                .onEntry(delegate::onValidateProtectedUrl)

        config.configure(State.VALIDATING_WHITELISTED_URL)
                .substateOf(State.VALIDATING_REQUESTED_URL)
                .permit(Event.WHITELISTED_URL, State.ACCESS_GRANTED)
                .permit(Event.RESTRICTED_URL, State.VALIDATING_RESTRICTED_METHOD)
                .onEntry(delegate::onValidateWhitelistedUrl)

        config.configure(State.VALIDATING_RESTRICTED_METHOD)
                .substateOf(State.VALIDATING_REQUESTED_URL)
                .permit(Event.UNRESTRICTED_METHOD, State.ACCESS_GRANTED)
                .permit(Event.RESTRICTED_METHOD, State.VALIDATING_TOKENS)
                .onEntry(delegate::onValidateRestrictedMethod)

        config.configure(State.VALIDATING_TOKENS)
                .substateOf(State.AUTHORIZING)
                .permit(Event.VALIDATE_ACCESS_TOKEN, State.VALIDATING_ACCESS_TOKEN)
                .onEntry(delegate::onStartValidateTokens)

        config.configure(State.VALIDATING_ACCESS_TOKEN)
                .substateOf(State.VALIDATING_TOKENS)
                .permitIf(Event.VALID_ACCESS_TOKEN, State.VALIDATING_SAME_SUBS) { !delegate.hasError }
                .permitIf(Event.INVALID_ACCESS_TOKEN, State.INVALID_TOKEN) { delegate.hasError }
                .onEntry(delegate::onValidateAccessToken)

        config.configure(State.VALIDATING_SAME_SUBS)
                .substateOf(State.VALIDATING_ACCESS_TOKEN)
                .permitIf(Event.VALID_SAME_SUBS, State.VALIDATING_PERMISSIONS) { !delegate.hasError }
                .permitIf(Event.INVALID_SAME_SUBS, State.INVALID_TOKEN) { delegate.hasError }
                .onEntry(delegate::onValidateSameSubs)

        config.configure(State.VALIDATING_PERMISSIONS)
                .substateOf(State.VALIDATING_ACCESS_TOKEN)
                .permitIf(Event.VALID_PERMISSIONS, State.VALIDATING_ID_TOKEN) { !delegate.hasError }
                .permitIf(Event.INVALID_PERMISSIONS, State.ACCESS_DENIED) { delegate.hasError }
                .onEntry(delegate::onValidatePermissions)

        config.configure(State.VALIDATING_ID_TOKEN)
                .substateOf(State.VALIDATING_TOKENS)
                .permitIf(Event.VALID_ID_TOKEN, State.ACCESS_GRANTED) { !delegate.hasError }
                .permitIf(Event.INVALID_ID_TOKEN, State.INVALID_TOKEN) { delegate.hasError }
                .onEntry(delegate::onValidateIdToken)

        config.configure(State.INVALID_TOKEN)
                .substateOf(State.VALIDATING_TOKENS)
                .permit(Event.IMMEDIATE_TRANSITION, State.NEED_REDIRECT)
                .onEntry(this::immediateTransition)

        config.configure(State.ERROR)
                .substateOf(State.AUTHORIZING)
                .onEntry(delegate::onError)

        config.configure(State.NEED_REDIRECT)
                .substateOf(State.AUTHORIZING)
                .onEntry(delegate::onNeedRedirect)

        config.configure(State.ACCESS_GRANTED)
                .substateOf(State.AUTHORIZING)
                .onEntry(delegate::onAccessGranted)

        config.configure(State.ACCESS_DENIED)
                .substateOf(State.AUTHORIZING)
                .onEntry(delegate::onAccessDenied)

        fsm = StateMachine(initialState, config)
        fsm.onUnhandledTrigger { _, _ -> /* ignore unhandled event */ }

        // print dotfile to stdout
        config.generateDotFileInto(System.err)
    }

    private fun immediateTransition() {
        fsm.fire(Event.IMMEDIATE_TRANSITION)
    }

    fun authorize(): State {
        fsm.fire(Event.AUTHORIZE)
        return fsm.state
    }


    /**
     * Post state machine event to internal queue.
     *
     * This design ensures that we're not triggering multiple events
     * from state machines callbacks before the transition is fully
     * completed.
     *
     * Method is re-entrant.
     */
    fun post(event: Event) {
        pendingEvents.addLast(event)
        if (!isProcessing) {
            isProcessing = true
            while (pendingEvents.isNotEmpty()) {
                val processedEvent = pendingEvents.removeFirst()
                fsm.fire(processedEvent)
            }
            isProcessing = false
        }
    }

    fun trace(message: String) {
        println(message)
    }
}
