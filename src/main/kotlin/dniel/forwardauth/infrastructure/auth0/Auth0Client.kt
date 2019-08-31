package dniel.forwardauth.infrastructure.auth0

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import dniel.forwardauth.AuthProperties
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.lang.IllegalStateException


@Component
class Auth0Client(val properties: AuthProperties) {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)
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
            LOGGER.debug("tokenRequest: "+JSON.writeValueAsString(tokenRequest))
        }

        val response: HttpResponse<JsonNode> = Unirest.post(TOKEN_ENDPOINT)
                .header("content-type", "application/json")
                .body(JSON.writeValueAsString(tokenRequest))
                .asJson();
        val status = response.status
        val body = response.body
        LOGGER.debug("Got response status $status from token endpoint");
        LOGGER.debug("BODY: " + body)

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