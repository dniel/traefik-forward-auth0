package dniel.forwardauth.infrastructure.endpoints

import org.slf4j.LoggerFactory
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class AppExceptionMapper : ExceptionMapper<WebApplicationException> {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    override fun toResponse(exception: WebApplicationException?): Response {
        val exceptionMessage = exception?.message ?: "Unknown Message."
        LOGGER.error("AppExceptionMapper.toResponse: error=${exceptionMessage}")
        return Response.status(Response.Status.BAD_REQUEST).entity("{ \"error\": \"$exceptionMessage\" }").build()
    }
}