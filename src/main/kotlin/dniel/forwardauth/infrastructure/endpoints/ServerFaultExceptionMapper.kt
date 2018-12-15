package dniel.forwardauth.infrastructure.endpoints

import org.slf4j.LoggerFactory
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class ServerFaultExceptionMapper : ExceptionMapper<RuntimeException> {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    override fun toResponse(exception: RuntimeException?): Response {
        val exceptionMessage = exception?.message ?: "Unknown Message."
        LOGGER.error("ServerFaultExceptionMapper.toResponse: error=${exceptionMessage}")
        return Response.serverError().entity("{ \"error\": \"$exceptionMessage\" }").build()
    }
}