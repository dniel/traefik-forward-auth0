package dniel.forwardauth.infrastructure.spring

import dniel.forwardauth.application.AuthorizeCommandHandler
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.stream.Collectors
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse


@RestController
class AuthorizeController(val authorizeCommandHandler: AuthorizeCommandHandler) {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    /**
     * Authorize Endpoint.
     * This endpoint is used by traefik forward properties to authorize requests.
     * It will return 200 for requests that has a valid JWT_TOKEN and will
     * redirect other to authenticate at Auth0.
     */
    @RequestMapping
    fun authorize(@RequestHeader headers: MultiValueMap<String, String>,
                  @CookieValue("ACCESS_TOKEN", required = false) accessTokenCookie: String?,
                  @CookieValue("JWT_TOKEN", required = false) userinfoCookie: String?,
                  @RequestHeader("x-forwarded-host") forwardedHostHeader: String,
                  @RequestHeader("x-forwarded-proto") forwardedProtoHeader: String,
                  @RequestHeader("x-forwarded-uri") forwardedUriHeader: String,
                  @RequestHeader("x-forwarded-method") forwardedMethodHeader: String,
                  response: HttpServletResponse): ResponseEntity<Unit> {

        printHeaders(headers)
        return authenticateToken(accessTokenCookie, userinfoCookie, forwardedMethodHeader, forwardedHostHeader, forwardedProtoHeader, forwardedUriHeader, response)
    }

    private fun authenticateToken(accessToken: String?, idToken: String?, method: String, host: String, protocol: String, uri: String, response: HttpServletResponse): ResponseEntity<Unit> {
        val command: AuthorizeCommandHandler.AuthorizeCommand = AuthorizeCommandHandler.AuthorizeCommand(accessToken, idToken, protocol, host, uri, method)
        val authorizeResult = authorizeCommandHandler.perform(command)
        return when {
            authorizeResult.isAuthenticated -> {
                val entity = ResponseEntity.ok()
                entity.header("Authorization", "Bearer ${accessToken}")
                authorizeResult.userinfo.forEach { k, v ->
                    val headerName = "X-FORWARDAUTH-${k.toUpperCase()}"
                    LOGGER.trace("Add header ${headerName} with value ${v}")
                    entity.header(headerName, v)
                }
                entity.build()
            }

            authorizeResult.isRestrictedUrl -> {
                LOGGER.debug("Redirect to ${authorizeResult.redirectUrl}")
                val nonceCookie = Cookie("AUTH_NONCE", authorizeResult.nonce.toString())
                nonceCookie.path = "/"
                nonceCookie.domain = authorizeResult.cookieDomain
                nonceCookie.isHttpOnly = true
                nonceCookie.maxAge = 7 * 24 * 60 * 60
                response.addCookie(nonceCookie)

                ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).location(authorizeResult.redirectUrl!!).build()
            }
            else -> ResponseEntity.noContent().build()
        }
    }


    private fun printHeaders(headers: MultiValueMap<String, String>) {
        if (LOGGER.isTraceEnabled) {
            headers.forEach { (key, value) -> LOGGER.trace(String.format("Header '%s' = %s", key, value.stream().collect(Collectors.joining("|")))) }
        }
    }
}