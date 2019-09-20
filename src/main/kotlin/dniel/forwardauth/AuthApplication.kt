package dniel.forwardauth

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER
import org.springframework.core.env.*
import java.util.stream.StreamSupport
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event
import java.util.*
import java.util.function.Consumer
import java.util.stream.Stream


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
/*    val env = applicationContext.getEnvironment()
    LOGGER.info("====== Environment and configuration ======")
    val sources = (env as AbstractEnvironment).propertySources

    LOGGER.info("===========================================")*/
}