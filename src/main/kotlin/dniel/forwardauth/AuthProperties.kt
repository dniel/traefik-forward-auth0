package dniel.forwardauth

import org.springframework.boot.context.properties.ConfigurationProperties
import javax.validation.constraints.NotBlank

@ConfigurationProperties()
class AuthProperties {
    lateinit var domain: String
    lateinit var tokenEndpoint: String
    lateinit var authorizeUrl: String

    val default = Application()
    val apps = ArrayList<Application>()

    class Application {
        lateinit var name: String
        lateinit var clientId: String
        lateinit var clientSecret: String
        lateinit var audience: String

        lateinit var scope: String
        lateinit var redirectUri: String
        lateinit var tokenCookieDomain: String

        override fun toString(): String {
            return "Application(name='$name', clientId='$clientId', clientSecret='$clientSecret', audience='$audience', scope='$scope', redirectUrl='$redirectUri', tokenCookieDomain='$tokenCookieDomain')"
        }
    }

    override fun toString(): String {
        return "AuthProperties(domain='$domain', tokenEndpoint='$tokenEndpoint', authorizeUrl='$authorizeUrl', default=$default, apps=$apps)"
    }


    fun findApplicationOrDefault(name: String): Application {
        return apps.find() { it.name == name } ?: default
    }
}