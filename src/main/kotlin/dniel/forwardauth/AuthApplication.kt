package dniel.forwardauth

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication


@SpringBootApplication
@EnableConfigurationProperties(AuthProperties::class)
open class AuthApplication(val properties: AuthProperties) {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    init {
        LOGGER.info(properties.toString());
    }

}

fun main(args: Array<String>) {
    val applicationContext = runApplication<AuthApplication>(*args)
/*    val env = applicationContext.getEnvironment()
    LOGGER.info("====== Environment and configuration ======")
    val sources = (env as AbstractEnvironment).propertySources
    LOGGER.info("===========================================")*/
}