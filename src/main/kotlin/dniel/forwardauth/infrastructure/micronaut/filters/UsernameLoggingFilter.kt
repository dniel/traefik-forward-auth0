package no.vy.trafficinfo.baseline.micronaut.system

import io.micronaut.core.order.Ordered
import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import io.micronaut.security.utils.SecurityService
import org.reactivestreams.Publisher
import org.slf4j.MDC
import reactor.core.publisher.Flux

@Filter(Filter.MATCH_ALL_PATTERN)
class UsernameLoggingFilter(private val securityService: SecurityService) : HttpServerFilter {

    override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE

    /**
     * Add username to logging.
     * If no user has been authenticated by security service set the username to anonymous.
     */
    override fun doFilter(request: HttpRequest<*>, chain: ServerFilterChain): Publisher<MutableHttpResponse<*>> {
        val user: String = if (request.userPrincipal.isPresent) request.userPrincipal.get().name else "anonymous"
        // propagate username to MDC context.
        MDC.put("user", user)

        return Flux.from(chain.proceed(request)).doOnNext {
            MDC.put("user", user)
        }.contextWrite {
            // propagate Reactor context from the HTTP filter to the controller’s coroutine:
            it.put("user", user)
        }
    }
}