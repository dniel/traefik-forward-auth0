package dniel.forwardauth.infrastructure.auth0

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mashape.unirest.http.Unirest
import dniel.forwardauth.AuthProperties
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class Auth0Service(val properties: AuthProperties) {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)
    val TOKEN_ENDPOINT = properties.tokenEndpoint
    val JSON = jacksonObjectMapper()

    /**
     * Call Auth0 to exchange received code with a JWT Token to decode.
     */
    fun exchangeCodeForToken(code: String, app: AuthProperties.Application): JSONObject? {
        LOGGER.info("Entered exchangeCodeForToken: code=$code")
        val tokenRequest = TokenRequest(
                code = code,
                clientId = app.clientId,
                clientSecret = app.clientSecret,
                redirectUrl = app.redirectUri)

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
    private data class TokenRequest(@JsonProperty("grant_type") val grantType: String = "authorization_code",
                                    @JsonProperty("client_id") val clientId: String,
                                    @JsonProperty("client_secret") val clientSecret: String,
                                    @JsonProperty("redirect_uri") val redirectUrl: String,
                                    val code: String,
                                    val scope: String = "openid profile"
    )

}