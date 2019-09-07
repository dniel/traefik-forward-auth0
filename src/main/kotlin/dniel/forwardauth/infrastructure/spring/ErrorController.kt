package dniel.forwardauth.infrastructure.spring

import org.slf4j.LoggerFactory
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest

/**
 * https://www.baeldung.com/spring-boot-custom-error-page
 */
@Controller
class ErrorController : ErrorController {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    @RequestMapping("/error")
    fun handleError(request: HttpServletRequest): String {
        val status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE).toString()
        status?.run {
            HttpStatus.valueOf(Integer.valueOf(this))
        }.let {
            return when (it) {
                HttpStatus.NOT_FOUND -> "error-404"
                HttpStatus.BAD_REQUEST -> "error-400"
                HttpStatus.FORBIDDEN -> "error-403"
                else -> {
                    "error"
                }
            }
        }
    }

    override fun getErrorPath(): String {
        return "/error"
    }
}