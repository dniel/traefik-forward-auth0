package dniel.forwardauth.infrastructure.spring.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.FORBIDDEN)
class PermissionDeniedException : Exception {
    constructor() : super("Permission denied to resource.")
    constructor(message: String) : super(message)
}