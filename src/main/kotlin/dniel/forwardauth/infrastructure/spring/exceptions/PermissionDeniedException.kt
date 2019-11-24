package dniel.forwardauth.infrastructure.spring.exceptions

import dniel.forwardauth.application.commandhandlers.AuthorizeHandler
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.FORBIDDEN)
class PermissionDeniedException : ApplicationException {
    constructor(error: AuthorizeHandler.AuthorizeEvent.AccessDenied) : super(error.reason)
}