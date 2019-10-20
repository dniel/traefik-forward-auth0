package dniel.forwardauth.infrastructure.spring.controllers

import org.slf4j.LoggerFactory
import org.springframework.util.MultiValueMap
import java.util.stream.Collectors
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

/**
 * Just a base class to provide some common functiosn for rest controllers.
 */
abstract class BaseController {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    fun addCookie(response: HttpServletResponse, name: String, value: String, domain: String, maxAge: Int) {
        val nonceCookie = Cookie(name, value)
        nonceCookie.domain = domain
        nonceCookie.maxAge = maxAge
        nonceCookie.isHttpOnly = true
        nonceCookie.path = "/"
        response.addCookie(nonceCookie)
    }

    fun clearCookie(response: HttpServletResponse, name: String, domain: String) {
        val nonceCookie = Cookie(name, "deleted")
        nonceCookie.domain = domain
        nonceCookie.maxAge = 0
        nonceCookie.path = "/"
        response.addCookie(nonceCookie)
    }

    fun trace(message: String) {
        LOGGER.trace(message)
    }

    fun error(message: String) {
        LOGGER.error(message)
    }

    fun printHeaders(headers: MultiValueMap<String, String>) {
        if (LOGGER.isTraceEnabled) {
            headers.forEach { (key, value) -> trace(String.format("Header '%s' = %s", key, value.stream().collect(Collectors.joining("|")))) }
        }
    }
}