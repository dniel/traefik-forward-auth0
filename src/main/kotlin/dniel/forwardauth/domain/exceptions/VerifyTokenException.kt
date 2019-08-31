package dniel.forwardauth.domain.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class VerifyTokenException(message: String) : Exception(message) {
}