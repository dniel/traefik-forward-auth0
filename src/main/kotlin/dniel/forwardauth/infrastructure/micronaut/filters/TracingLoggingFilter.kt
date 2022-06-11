package no.vy.trafficinfo.baseline.micronaut.system

import io.micronaut.core.order.Ordered
import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import java.util.UUID
import org.reactivestreams.Publisher
import org.slf4j.MDC
import reactor.core.publisher.Flux

@Filter(Filter.MATCH_ALL_PATTERN)
class TracingLoggingFilter : HttpServerFilter {

    override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE

    /**
     * Add TRACING uuid to request and return the tracing id in http response.
     */
    override fun doFilter(request: HttpRequest<*>, chain: ServerFilterChain): Publisher<MutableHttpResponse<*>> {
        // create a random uuid as traceid if none sent in headers.
        val uuid: String = request.headers["X-TRACE-ID"] ?: UUID.randomUUID().toString()

        // put the trace uuid in MDC to use in logging.
        MDC.put("trace", uuid)

        return Flux.from(chain.proceed(request)).doOnNext { res ->
            // add X-TRACE-ID to response to use in subsequent calls
            res.headers["X-TRACE-ID"] = uuid
        }.contextWrite {
            // propagate Reactor context from the HTTP filter to the controller’s coroutine:
            it.put("tracingId", uuid)
        }
    }
}