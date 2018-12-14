package dniel.forwardauth

import org.slf4j.LoggerFactory
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class AuthExceptionMapper : ExceptionMapper<Exception> {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    override fun toResponse(exception: Exception?): Response {
        val exceptionMessage = exception?.message ?: "Unknown Message."
        LOGGER.debug("MapExceptionToResponse: ${exceptionMessage}")
        return Response.ok("{ \"error\": \"$exceptionMessage\" }").build()
    }
}