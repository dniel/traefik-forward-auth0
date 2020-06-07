package dniel.forwardauth.domain.shared

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*
import javax.validation.constraints.NotEmpty

class Application {
    @NotEmpty
    lateinit var name: String

    @get:JsonIgnore
    var clientId: String = ""

    @get:JsonIgnore
    var clientSecret: String = ""
    var audience: String = ""
    var scope: String = "profile openid email"
    var redirectUri: String = ""
    var tokenCookieDomain: String = ""
    var restrictedMethods: Array<String> = arrayOf("DELETE", "GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT")
    var requiredPermissions: Array<String> = emptyArray()
    var claims: Array<String> = emptyArray()
    var returnTo: String = ""

    override fun toString(): String {
        return "Application(name='$name', clientId='$clientId', clientSecret='$clientSecret', audience='$audience', scope='$scope', redirectUri='$redirectUri', tokenCookieDomain='$tokenCookieDomain', restrictedMethods=${Arrays.toString(restrictedMethods)}, requiredPermissions=${Arrays.toString(requiredPermissions)}, claims=${Arrays.toString(claims)})"
    }

}