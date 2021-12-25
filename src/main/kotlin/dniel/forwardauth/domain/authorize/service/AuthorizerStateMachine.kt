/*
 * Copyright (c)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dniel.forwardauth.domain.authorize.service

import com.github.oxo42.stateless4j.StateMachine
import com.github.oxo42.stateless4j.StateMachineConfig
import org.slf4j.LoggerFactory


/**
 * Generated from dot file generator.
 *
 * digraph G {
 *  VALIDATING_ID_TOKEN -> ACCESS_GRANTED;
 *  VALIDATING_ID_TOKEN -> INVALID_TOKEN;
 *  VALIDATING_TOKENS -> VALIDATING_ACCESS_TOKEN;
 *  AUTHORIZING -> ERROR;
 *  AUTHORIZING -> VALIDATING_REQUESTED_URL;
 *  VALIDATING_ACCESS_TOKEN -> INVALID_TOKEN;
 *  VALIDATING_ACCESS_TOKEN -> VALIDATING_SAME_SUBS;
 *  AWAIT_AUTHORIZING -> AUTHORIZING;
 *  VALIDATING_SAME_SUBS -> VALIDATING_PERMISSIONS;
 *  VALIDATING_SAME_SUBS -> INVALID_TOKEN;
 *  VALIDATING_REQUESTED_URL -> VALIDATING_WHITELISTED_URL;
 *  VALIDATING_WHITELISTED_URL -> ACCESS_GRANTED;
 *  VALIDATING_WHITELISTED_URL -> VALIDATING_RESTRICTED_METHOD;
 *  VALIDATING_RESTRICTED_METHOD -> ACCESS_GRANTED;
 *  VALIDATING_RESTRICTED_METHOD -> VALIDATING_TOKENS;
 *  VALIDATING_PERMISSIONS -> VALIDATING_ID_TOKEN;
 *  VALIDATING_PERMISSIONS -> ACCESS_DENIED;
 *  INVALID_TOKEN -> NEED_REDIRECT;
 *  INVALID_TOKEN -> ACCESS_DENIED;
 * }
 *
 */
class AuthorizerStateMachine(private val delegate: Delegate) {
    val LOGGER = LoggerFactory.getLogger(this::class.java)

    interface Delegate {
        val hasError: Boolean
        val isApi: Boolean

        fun onStartAuthorizing()
        fun onStartValidateTokens()
        fun onError()
        fun onValidateAccessToken()
        fun onValidatePermissions()
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

        VALIDATING_ACCESS_TOKEN,
        VALIDATING_PERMISSIONS,

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

        // verify url and http method
        VALIDATE_REQUESTED_URL,
        VALIDATE_WHITELISTED_URL,

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

        // generic error event
        ERROR,
        NEXT_TRANSITION
    }

    private var pendingEvents = ArrayDeque<Event>()
    private var isProcessing = false
    private val fsm: StateMachine<State, Event>

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
                .permitIf(Event.VALIDATE_ACCESS_TOKEN, State.VALIDATING_ACCESS_TOKEN) { !delegate.hasError }
                .onEntry(delegate::onStartValidateTokens)

        config.configure(State.VALIDATING_ACCESS_TOKEN)
                .substateOf(State.VALIDATING_TOKENS)
                .permitIf(Event.VALID_ACCESS_TOKEN, State.VALIDATING_PERMISSIONS) { !delegate.hasError }
                .permit(Event.INVALID_ACCESS_TOKEN, State.INVALID_TOKEN)
                .onEntry(delegate::onValidateAccessToken)

        config.configure(State.VALIDATING_PERMISSIONS)
                .substateOf(State.VALIDATING_TOKENS)
                .permitIf(Event.VALID_PERMISSIONS, State.ACCESS_GRANTED) { !delegate.hasError }
                .permit(Event.INVALID_PERMISSIONS, State.ACCESS_DENIED)
                .onEntry(delegate::onValidatePermissions)

        config.configure(State.INVALID_TOKEN)
                .substateOf(State.VALIDATING_TOKENS)
                .permitIf(Event.NEXT_TRANSITION, State.NEED_REDIRECT) { !delegate.isApi }
                .permitIf(Event.NEXT_TRANSITION, State.ACCESS_DENIED) { delegate.isApi }
                .onEntry(this::nextTransition)

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

        fsm = StateMachine(State.AWAIT_AUTHORIZING, config)
        fsm.onUnhandledTrigger { _, _ -> /* ignore unhandled event */ }

//        config.generateDotFileInto(System.err)
    }

    private fun nextTransition() {
        fsm.fire(Event.NEXT_TRANSITION)
    }

    fun authorize(): State {
        fsm.fire(Event.AUTHORIZE)
        return fsm.state
    }

    /**
     * Post authorizeState machine event to internal queue.
     *
     * This design ensures that we're not triggering multiple events
     * from authorizeState machines callbacks before the transition is fully
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
}
