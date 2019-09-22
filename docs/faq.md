# Frequently asked Questions

## Q: Why is my access token not a JWT?
See https://community.auth0.com/t/why-is-my-access-token-not-a-jwt/31028

## Q: How do I check if ForwardAuth is running?
ForwardAuth should be accessible through traefik on https://auth.domain.com/actuator/info.  
That endpoint should give you some info about which version of ForwardAuth is running.
If that endpoint is reachable then the other endpoints of ForwardAuth should also be
reachable and it should be possible to login from auth0.
