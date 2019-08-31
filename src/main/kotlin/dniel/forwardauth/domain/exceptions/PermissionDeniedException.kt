package dniel.forwardauth.domain.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.FORBIDDEN)
class PermissionDeniedException : Exception("Permission denied to resource.") {
}