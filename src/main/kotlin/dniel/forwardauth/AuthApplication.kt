package dniel.forwardauth

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication


@OpenAPIDefinition(
        info = Info(
                title = "ForwardAuth for Auth0 API",
                version = "v2",
                description = "ForwardAuth for Auth0",
                contact = Contact(
                        name = "Daniel",
                        email = "daniel@engfeldt.net",
                        url = "http://github.com/dniel"
                )
        )
)
@SecurityScheme(
        name = "forwardauth",
        paramName = "ACCESS_TOKEN",
        type = SecuritySchemeType.OAUTH2,
        `in` = SecuritySchemeIn.COOKIE,
        bearerFormat = "jwt"
)
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