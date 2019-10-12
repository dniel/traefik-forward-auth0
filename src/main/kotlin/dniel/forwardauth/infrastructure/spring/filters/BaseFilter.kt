package dniel.forwardauth.infrastructure.spring.filters

import org.slf4j.LoggerFactory
import org.springframework.web.filter.OncePerRequestFilter

abstract class BaseFilter() : OncePerRequestFilter() {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    fun trace(message: String) {
        LOGGER.trace(message)
    }

}