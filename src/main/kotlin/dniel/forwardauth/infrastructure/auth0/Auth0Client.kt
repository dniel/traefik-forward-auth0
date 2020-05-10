package dniel.forwardauth.infrastructure.auth0

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.cache.CacheBuilder
import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import dniel.forwardauth.AuthProperties
import org.apache.http.HttpStatus
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Auth0 Client.
 * Creates HTTP calls to the Auth0 HTTP API and handles the results.
 *
 */
@Component
class Auth0Client(val properties: AuthProperties) {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)
    val LOGOUT_ENDPOINT = properties.logoutEndpoint
    val USERINFO_ENDPOINT = properties.userinfoEndpoint
    val TOKEN_ENDPOINT = properties.tokenEndpoint
    val JSON = jacksonObjectMapper()

    /**
     * Object to cache, contains both the received token and when it expires for refresh.
     * @param accessToken is the token that is cached
     * @param expires is milliseconds since epoch for when the token expires.
     */
    data class CachedToken(val accessToken: JSONObject, val expires: Long) {
        fun hasExpired(): Boolean = System.currentTimeMillis() > expires
    }

    // Hold already retrived tokens in cache for client_credentials to avoid excessive api calls to Auth0.
    val cache = CacheBuilder.newBuilder().expireAfterAccess(24, TimeUnit.HOURS).build<Int, CachedToken>()

    /**
     * Call Auth0 to exchange received code with a JWT Token to decode.
     *
     * @param code from Auth0
     * @param clientId from Auth0 Application
     * @param clientSecret from Auth0 Application
     * @param redirectUri from Auth0 Application.
     * @return JSON object with access_token and id_token.
     * @throws IllegalStateException if error was found in response from API call.
     */
    fun authorizationCodeExchange(code: String, clientId: String, clientSecret: String, redirectUri: String): JSONObject {
        LOGGER.debug("Perform AuthorizationCodeExchange:  code=$code")
        val tokenRequest = AuthorizationCodeTokenRequest(
                code = code,
                clientId = clientId,
                clientSecret = clientSecret,
                redirectUrl = redirectUri)

        LOGGER.trace("tokenRequest: " + JSON.writeValueAsString(tokenRequest))
        val response: HttpResponse<JsonNode> = Unirest.post(TOKEN_ENDPOINT)
                .header("content-type", "application/json")
                .body(JSON.writeValueAsString(tokenRequest))
                .asJson();

        return handleResponse(response)
    }

    /**
     * Call Auth0 with clientid and clientsecret to get Access Token in return.
     * Will cache the resulting access_token per clientid for 24hours to reduce
     * number if API calls to Auth0.
     *
     * <p>
     *     Example of a successful response with access_token
     *     <pre>{@code
     *       {
     *         "access_token":"eyJz93a...k4laUWw",
     *         "token_type":"Bearer",
     *         "expires_in":86400
     *       }
     *    }</pre>
     * </p>
     *
     * <p>
     *     Example of an error message from Auth0.
     *     <pre>{@code
     *       {
     *       "error_description": "Client is not authorized to access \"https://example.com\". ...",
     *       "error": "access_denied"
     *       }
     *     }</pre>
     * </p>
     *
     * @param clientId from Auth0 Application
     * @param clientSecret from Auth0 Application
     * @return json object that contains a key "access_token"
     */
    fun clientCredentialsExchange(clientId: String, clientSecret: String, audience: String): JSONObject {
        LOGGER.debug("Perform Client Credentials Exchange client-id: ${clientId}")
        var cachedToken = requestClientCredentialsToken(clientId, clientSecret, audience)

        // if cached token has expired, invalidate it and reload from Auth0.
        if (cachedToken.hasExpired()) {
            LOGGER.trace("Token has expired in cache, invalidate and re-request a new one from Auth0.")
            cache.invalidate(cachedToken)
            cachedToken = requestClientCredentialsToken(clientId, clientSecret, audience)
        } else {
            LOGGER.trace("Token has not yet expired, valid until ${Date(cachedToken.expires)}")
        }

        return cachedToken.accessToken
    }

    /**
     * Request access token by client credentials HTTP API call.
     * Result is cached for 24 hours using the hashcode of
     * the combination of clientid, clientsect and audience.
     */
    private fun requestClientCredentialsToken(clientId: String, clientSecret: String, audience: String) =
            cache.get((clientId+clientSecret+audience).hashCode()) {
                LOGGER.trace("Request Access Token by Client Credentials from Auth0.")
                val tokenRequest = ClientCredentialsTokenRequest(
                        clientId = clientId,
                        clientSecret = clientSecret,
                        audience = audience)

                LOGGER.trace("tokenRequest: " + JSON.writeValueAsString(tokenRequest))
                val response = Unirest.post(TOKEN_ENDPOINT)
                        .header("content-type", "application/json")
                        .body(JSON.writeValueAsString(tokenRequest))
                        .asJson();

                val jsonObject = handleResponse(response)
                val expiresIn = jsonObject.getInt("expires_in")

                CachedToken(jsonObject, System.currentTimeMillis() + (expiresIn * 1000))
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
     * From the Auth0 documentation:
     * This endpoint will work only if openid was granted as a scope for the Access Token.
     * The user profile information included in the response depends on the scopes requested.
     * For example, a scope of just openid may return less information than a a scope of openid profile email
     *
     * See the Auth0 documentation.
     * @link https://auth0.com/docs/api/user#get-user-info
     */
    fun userinfo(accesstoken: String): Map<String, Any> {
        LOGGER.debug("Perform retrieve Userinfo from endpoint: ${USERINFO_ENDPOINT}")
        val response = Unirest
                .get(USERINFO_ENDPOINT)
                .header("Authorization", "Bearer ${accesstoken}")
                .asJson()

        val jsonObject = handleResponse(response)
        val userinfo = mutableMapOf<String, Any>()
        jsonObject.keys().forEach { key -> userinfo.put(key, jsonObject.get(key)) }
        return userinfo
    }

    /**
     * Handle response payload from Auth0.
     *
     * @throws IllegalStateException if error was found in payload.
     * @return JSONObject with payload if no error found.
     */
    private fun handleResponse(response: HttpResponse<JsonNode>): JSONObject {
        val status = response.status
        val body = response.body
        LOGGER.trace("Response status: ${status}")
        LOGGER.trace("Response body: ${body}")

        if (body.`object`.has("error")) {
            val error = body.`object`.getString("error")
            val errorDesc = body.`object`.getString("error_description")
            throw IllegalStateException("Received error in response from Auth0: $error, $errorDesc")
        }
        return body.`object`
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