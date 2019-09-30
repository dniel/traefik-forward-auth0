package dniel.forwardauth.infrastructure.spring.exceptions

import dniel.forwardauth.application.AuthorizeHandler
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Generic Authorization Error.
 * Should stop execution and not give access to be sure that we dont give access to someone that shouldnt be allowed.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
open class AuthorizationException : ApplicationException {
    constructor(error: AuthorizeHandler.AuthEvent.Error) : super(error.reason)
}