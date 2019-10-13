# Frequently asked Questions

## Q: Why Opaque Access Tokens is not supported?
The only way for ForwardAuth to validate that the Access Token has not been tampered with and has not expired is if you
use a token with JWT format See https://community.auth0.com/t/why-is-my-access-token-not-a-jwt/31028

## Q: How do I check if ForwardAuth is accessible through Traefik?
After completing the installation instructions, ForwardAuth should be accessible through Traefik on 
https://auth.domain.com/actuator/info.  
That endpoint should give you some info about which version of ForwardAuth is running.
If that endpoint is reachable then the other endpoints of ForwardAuth should also be
reachable and it should be possible to login from auth0.

## Q: How do I adjust loglevel in application?
By setting the environment variable `ENV` for ForwardAuth to DEV, TEST or PRODUCTION different levels of
logging is used by the application.

## Q: Updates in permissions/api/applications does not show in ForwardAuth and I still get permission denied?
The user profile and permissions is requested when doing first login and until the token has expired it will
not get updated with changes done in Auth0. So if you add or remove something it will not be reflected until 
you re-authenticate again. To force a new session you could do one of the following, 
- remove cookies from current session
- open a private browser session to start with blank cookies
- call the https://auth.exmaple.com/signout endpoint to both sign out of auth0 and delete cookies.