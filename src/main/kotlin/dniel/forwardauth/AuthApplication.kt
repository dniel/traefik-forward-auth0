package dniel.forwardauth

import dniel.forwardauth.infrastructure.endpoints.AppExceptionMapper
import dniel.forwardauth.infrastructure.endpoints.AuthorizeEndpoint
import dniel.forwardauth.infrastructure.endpoints.ServerFaultExceptionMapper
import dniel.forwardauth.infrastructure.endpoints.SigninEndpoint
import org.glassfish.jersey.server.ResourceConfig
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication


@SpringBootApplication
@EnableConfigurationProperties(AuthProperties::class)
class AuthApplication(val auth: AuthProperties) : ResourceConfig() {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    init {
        register(AppExceptionMapper::class.java)
        register(ServerFaultExceptionMapper::class.java)
        register(AuthorizeEndpoint::class.java)
        register(SigninEndpoint::class.java)

        LOGGER.info(auth.toString());
    }

}

fun main(args: Array<String>) {
    val applicationContext = runApplication<AuthApplication>(*args)
}