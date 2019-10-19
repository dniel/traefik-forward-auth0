package dniel.forwardauth

import dniel.forwardauth.domain.shared.Application
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import java.util.*
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
    lateinit var logoutEndpoint: String

    @NotEmpty
    lateinit var userinfoEndpoint: String

    @NotEmpty
    lateinit var authorizeUrl: String

    @NotNull
    val default = Application()

    @NotNull
    val apps = ArrayList<Application>()

    override fun toString(): String {
        return "AuthProperties(domain='$domain', tokenEndpoint='$tokenEndpoint', authorizeUrl='$authorizeUrl', default=$default, apps=$apps)"
    }

    /**
     * Return application with application specific values, default values or inherited values.
     */
    fun findApplicationOrDefault(name: String?): Application {
        if (name == null) return default;

        val application = apps.find() { it.name.equals(name, ignoreCase = true) }
        if (application !== null) {
            application.returnTo = if (application.returnTo.isNotEmpty()) application.returnTo else default.returnTo
            application.redirectUri = if (application.redirectUri.isNotEmpty()) application.redirectUri else default.redirectUri
            application.audience = if (application.audience.isNotEmpty()) application.audience else default.audience
            application.scope = if (application.scope.isNotEmpty()) application.scope else default.scope
            application.clientId = if (application.clientId.isNotEmpty()) application.clientId else default.clientId
            application.clientSecret = if (application.clientSecret.isNotEmpty()) application.clientSecret else default.clientSecret
            application.tokenCookieDomain = if (application.tokenCookieDomain.isNotEmpty()) application.tokenCookieDomain else default.tokenCookieDomain
            application.restrictedMethods = if (application.restrictedMethods.isNotEmpty()) application.restrictedMethods else default.restrictedMethods
            application.claims = if (application.claims.isNotEmpty()) application.claims else default.claims
            application.requiredPermissions = if (application.requiredPermissions.isNotEmpty()) application.requiredPermissions else default.requiredPermissions
            return application
        } else return default;
    }
}