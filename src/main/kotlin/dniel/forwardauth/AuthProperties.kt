package dniel.forwardauth

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Validated
@ConfigurationProperties()
class AuthProperties {
    @NotEmpty
    lateinit var domain: String

    @NotEmpty
    lateinit var tokenEndpoint: String

    @NotEmpty
    lateinit var authorizeUrl: String

    @NotNull
    val default = Application()

    val apps = ArrayList<Application>()

    class Application {
        @NotEmpty
        lateinit var name: String

        @NotEmpty
        lateinit var clientId: String

        @NotEmpty
        lateinit var clientSecret: String

        @NotEmpty
        lateinit var audience: String

        @NotEmpty
        lateinit var scope: String

        @NotEmpty
        lateinit var redirectUri: String

        @NotEmpty
        lateinit var tokenCookieDomain: String

        override fun toString(): String {
            return "Application(name='$name', clientId='$clientId', clientSecret='$clientSecret', audience='$audience', scope='$scope', redirectUrl='$redirectUri', tokenCookieDomain='$tokenCookieDomain')"
        }
    }

    override fun toString(): String {
        return "AuthProperties(domain='$domain', tokenEndpoint='$tokenEndpoint', authorizeUrl='$authorizeUrl', default=$default, apps=$apps)"
    }


    fun findApplicationOrDefault(name: String?): Application {
        if (name == null) return default;
        return apps.find() { it.name == name } ?: default
    }
}