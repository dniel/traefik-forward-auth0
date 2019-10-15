package dniel.forwardauth.infrastructure.spring.exceptions

import dniel.forwardauth.application.AuthenticateHandler
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Generic Authentication Error.
 * Should stop execution and not give access to be sure that we dont give access to someone that shouldnt be allowed.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
open class AuthenticationException : ApplicationException {
    constructor(error: AuthenticateHandler.AuthentiationEvent.Error) : super(error.reason)
}