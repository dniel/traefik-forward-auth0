package dniel.forwardauth

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication


@SpringBootApplication
@EnableConfigurationProperties(AuthProperties::class)
open class AuthApplication(val auth: AuthProperties) {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    init {
        LOGGER.info(auth.toString());
    }

}

fun main(args: Array<String>) {
    val applicationContext = runApplication<AuthApplication>(*args)
}