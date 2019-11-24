package dniel.forwardauth.infrastructure.auth0

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import dniel.forwardauth.AuthProperties
import org.apache.http.HttpStatus
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


@Component
class Auth0Client(val properties: AuthProperties) {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)
    val LOGOUT_ENDPOINT = properties.logoutEndpoint
    val USERINFO_ENDPOINT = properties.userinfoEndpoint
    val TOKEN_ENDPOINT = properties.tokenEndpoint
    val JSON = jacksonObjectMapper()

    /**
     * Call Auth0 to exchange received code with a JWT Token to decode.
     */
    fun authorizationCodeExchange(code: String, clientId: String, clientSecret: String, redirectUri: String): JSONObject {
        LOGGER.info("AuthorizationCodeExchange:  code=$code")
        val tokenRequest = AuthorizationCodeTokenRequest(
                code = code,
                clientId = clientId,
                clientSecret = clientSecret,
                redirectUrl = redirectUri)

        if (LOGGER.isDebugEnabled) {
            LOGGER.debug("tokenRequest: " + JSON.writeValueAsString(tokenRequest))
        }

        val response: HttpResponse<JsonNode> = Unirest.post(TOKEN_ENDPOINT)
                .header("content-type", "application/json")
                .body(JSON.writeValueAsString(tokenRequest))
                .asJson();
        val status = response.status
        val body = response.body
        LOGGER.trace("Response status: ${status}")
        LOGGER.trace("Response body: ${body}")

        if (body.`object`.has("error")) {
            val error = body.`object`.getString("error")
            val errorDesc = body.`object`.getString("error_description")
            throw IllegalStateException("Received error in Code Exchange response from Auth0: $error, $errorDesc")
        }

        return response.getBody().getObject()
    }

    /**
     * Call Auth0 to exchange received code with a JWT Token to decode.
     */
    fun clientCredentialsExchange(clientId: String, clientSecret: String, audience: String): JSONObject {
        LOGGER.info("Entered clientCredentialsExchange")
        val tokenRequest = ClientCredentialsTokenRequest(
                clientId = clientId,
                clientSecret = clientSecret,
                audience = audience)

        val response = Unirest.post(TOKEN_ENDPOINT)
                .header("content-type", "application/json")
                .body(JSON.writeValueAsString(tokenRequest))
                .asJson();

        LOGGER.info("response: " + response.toString())
        return response.getBody().getObject()
    }

    /**
     * Call Auth0 to exchange received code with a JWT Token to decode.
     * @param clientId the application to sign out from.
     * @param returnTo the url to redirect to after the signout has been completed.
     * @return url to redirect to if one is requested, or empty if no redirect returned.
     */
    fun signout(clientId: String, returnTo: String): String? {
        LOGGER.debug("Perform Sign Out")
        Unirest.setHttpClient(org.apache.http.impl.client.HttpClients.custom()
                .disableRedirectHandling()
                .build())

        LOGGER.trace("Request Signout Endpoint: ${LOGOUT_ENDPOINT}")
        val response = with(Unirest.get(LOGOUT_ENDPOINT)) {
            queryString("client_id", clientId)
            queryString("returnTo", returnTo)
        }.asString()
        LOGGER.trace("Response status: ${response.status}")
        LOGGER.trace("Response body: ${response.body}")

        return when (response.status) {
            HttpStatus.SC_MOVED_TEMPORARILY -> response.headers.getFirst("Location")
            HttpStatus.SC_BAD_REQUEST -> throw IllegalArgumentException("Illegal arguments to sign out at Auth0, check configuration.")
            HttpStatus.SC_OK -> return null
            else -> return null
        }
    }

    /**
     * Call auth0 and retrieve Userinfo
     *
     * See the Auth0 documentation.
     * @link https://auth0.com/docs/api/user#get-user-info
     */
    fun userinfo(accesstoken: String): Map<String, Any> {
        LOGGER.trace("Request Userinfo Endpoint: ${USERINFO_ENDPOINT}")
        val response = Unirest
                .get(USERINFO_ENDPOINT)
                .header("Authorization", "Bearer ${accesstoken}")
                .asJson()

        LOGGER.trace("Response status: ${response.status}")
        LOGGER.trace("Response body: ${response.body}")

        val jsonObject = response.body.`object`
        val userinfo = mutableMapOf<String, Any>()
        jsonObject.keys().forEach { key -> userinfo.put(key, jsonObject.get(key)) }
        return userinfo
    }

    /**
     * Just a simple data class for the token request.
     */
    private data class AuthorizationCodeTokenRequest(@JsonProperty("grant_type") val grantType: String = "authorization_code",
                                                     @JsonProperty("client_id") val clientId: String,
                                                     @JsonProperty("client_secret") val clientSecret: String,
                                                     @JsonProperty("redirect_uri") val redirectUrl: String,
                                                     val code: String,
                                                     val scope: String = "openid id_token"
    )

    /**
     * Just a simple data class for the token request.
     */
    private data class ClientCredentialsTokenRequest(@JsonProperty("grant_type") val grantType: String = "client_credentials",
                                                     @JsonProperty("client_id") val clientId: String,
                                                     @JsonProperty("client_secret") val clientSecret: String,
                                                     @JsonProperty("audience") val audience: String

    )

}