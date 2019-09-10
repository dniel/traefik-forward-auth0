package dniel.forwardauth.infrastructure.spring.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Generic error, but should stop execution and not give access to be sure
 * that we dont give access to someone that shouldnt be allowed.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class Auth0Exception : ApplicationErrorException {
    constructor(error: String, description: String) : super("${error}, ${description}")

}