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

package dniel.forwardauth.domain.authenticate.service

import com.github.oxo42.stateless4j.StateMachine
import com.github.oxo42.stateless4j.StateMachineConfig
import org.slf4j.LoggerFactory


/**
 * Generated from dot file generator.
 *
 * digraph G {
 * AUTHENTICATING -> VALIDATING_TOKENS;
 *  VALIDATING_ID_TOKEN -> INVALID_TOKEN;
 *  VALIDATING_ID_TOKEN -> VALIDATING_SAME_SUBS;
 *  VALID_TOKENS -> AUTHENTICATED;
 *  INVALID_TOKEN -> ANONYMOUS;
 *  VALIDATING_ACCESS_TOKEN -> VALIDATING_ID_TOKEN;
 *  VALIDATING_ACCESS_TOKEN -> INVALID_TOKEN;
 *  VALIDATING_SAME_SUBS -> INVALID_TOKEN;
 *  VALIDATING_SAME_SUBS -> VALID_TOKENS;
 *  AWAIT_AUTHENTICATION -> AUTHENTICATING;
 *  VALIDATING_TOKENS -> VALIDATING_ACCESS_TOKEN;
 * }
 *
 *
 */
class AuthenticatorStateMachine(private val delegate: Delegate) {
    val LOGGER = LoggerFactory.getLogger(this::class.java)

    interface Delegate {
        val hasError: Boolean

        fun onStartAuthentication()
        fun onStartValidateTokens()
        fun onError()
        fun onValidateAccessToken()
        fun onValidateIdToken()
        fun onValidateSameSubs()
        fun onInvalidToken()
        fun onAuthenticated()
        fun onAnonymous()
    }

    enum class State {
        AWAIT_AUTHENTICATION,
        AUTHENTICATING,
        VALIDATING_TOKENS,
        VALIDATING_ID_TOKEN,
        VALIDATING_ACCESS_TOKEN,
        VALIDATING_SAME_SUBS,
        INVALID_TOKEN,
        VALID_TOKENS,
        AUTHENTICATED,
        ANONYMOUS,
        ERROR
    }

    enum class Event {
        AUTHENTICATE,

        // validate the tokens.
        VALIDATE_TOKENS,
        VALIDATE_ACCESS_TOKEN,
        VALID_ACCESS_TOKEN,
        INVALID_ACCESS_TOKEN,
        INVALID_SUBS,
        VALID_SUBS,

        EMPTY_ID_TOKEN,
        VALID_ID_TOKEN,
        INVALID_ID_TOKEN,
        ERROR,
        NEXT_STATE
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

        config.configure(State.AWAIT_AUTHENTICATION)
                .permit(Event.AUTHENTICATE, State.AUTHENTICATING)

        config.configure(State.AUTHENTICATING)
                .permit(Event.VALIDATE_TOKENS, State.VALIDATING_TOKENS)
                .onEntry(delegate::onStartAuthentication)

        config.configure(State.VALIDATING_TOKENS)
                .substateOf(State.AUTHENTICATING)
                .permit(Event.VALIDATE_ACCESS_TOKEN, State.VALIDATING_ACCESS_TOKEN)
                .onEntry(delegate::onStartValidateTokens)

        config.configure(State.VALIDATING_ACCESS_TOKEN)
                .substateOf(State.VALIDATING_TOKENS)
                .permitIf(Event.VALID_ACCESS_TOKEN, State.VALIDATING_ID_TOKEN) { !delegate.hasError }
                .permitIf(Event.INVALID_ACCESS_TOKEN, State.INVALID_TOKEN) { delegate.hasError }
                .onEntry(delegate::onValidateAccessToken)

        config.configure(State.VALIDATING_ID_TOKEN)
                .substateOf(State.VALIDATING_TOKENS)
                .permitIf(Event.EMPTY_ID_TOKEN, State.VALID_TOKENS) { !delegate.hasError }
                .permitIf(Event.VALID_ID_TOKEN, State.VALIDATING_SAME_SUBS) { !delegate.hasError }
                .permitIf(Event.INVALID_ID_TOKEN, State.INVALID_TOKEN) { delegate.hasError }
                .onEntry(delegate::onValidateIdToken)

        config.configure(State.VALIDATING_SAME_SUBS)
                .substateOf(State.VALIDATING_TOKENS)
                .permitIf(Event.VALID_SUBS, State.VALID_TOKENS) { !delegate.hasError }
                .permitIf(Event.INVALID_SUBS, State.INVALID_TOKEN) { delegate.hasError }
                .onEntry(delegate::onValidateSameSubs)

        config.configure(State.INVALID_TOKEN)
                .substateOf(State.VALIDATING_TOKENS)
                .permit(Event.NEXT_STATE, State.ANONYMOUS)
                .onEntry(this::nextState)

        config.configure(State.VALID_TOKENS)
                .substateOf(State.VALIDATING_TOKENS)

                .permit(Event.NEXT_STATE, State.AUTHENTICATED)
                .onEntry(this::nextState)

        config.configure(State.ERROR)
                .substateOf(State.AUTHENTICATING)
                .onEntry(delegate::onError)

        config.configure(State.AUTHENTICATED)
                .substateOf(State.AUTHENTICATING)
                .onEntry(delegate::onAuthenticated)

        config.configure(State.ANONYMOUS)
                .substateOf(State.AUTHENTICATING)
                .onEntry(delegate::onAnonymous)

        fsm = StateMachine(State.AWAIT_AUTHENTICATION, config)
        fsm.onUnhandledTrigger { _, _ -> /* ignore unhandled event */ }

        // print dotfile to stdout
        // config.generateDotFileInto(System.err)
    }

    private fun nextState() {
        fsm.fire(Event.NEXT_STATE)
    }

    /**
     * Start step of the authentication process.
     */
    fun authenticate(): State {
        fsm.fire(Event.AUTHENTICATE)
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
