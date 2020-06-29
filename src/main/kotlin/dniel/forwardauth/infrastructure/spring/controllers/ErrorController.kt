package dniel.forwardauth.infrastructure.spring.controllers

import dniel.forwardauth.infrastructure.spring.exceptions.PermissionDeniedException
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver
import org.springframework.boot.web.servlet.error.ErrorAttributes
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


//@Component
class ErrorController(errorAttributes: ErrorAttributes?,
                      serverProperties: ServerProperties,
                      errorViewResolvers: List<ErrorViewResolver?>?) : BasicErrorController(errorAttributes, serverProperties.error, errorViewResolvers) {

    override fun errorHtml(request: HttpServletRequest?, response: HttpServletResponse?): ModelAndView {
        response!!.setHeader("testHtml", "test")
        return super.errorHtml(request, response)
    }

    override fun error(request: HttpServletRequest): ResponseEntity<Map<String, Any>> {
        val body = getErrorAttributes(request,
                isIncludeStackTrace(request, MediaType.ALL))
        val status = getStatus(request)
        val headers = HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8)
        headers.put("test", listOf("test"))
        return ResponseEntity(body, headers, status)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(this.javaClass)
    }

    init {
        LOGGER.info("Created")
    }
}