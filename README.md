[![Known Vulnerabilities](https://snyk.io/test/github/dniel/traefik-forward-auth0/badge.svg)](https://snyk.io/test/github/dniel/traefik-forward-auth0)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=dniel_traefik-forward-auth0&metric=alert_status)](https://sonarcloud.io/dashboard?id=dniel_traefik-forward-auth0)

# ForwardAuth for Traefik
This is a SpringBoot backend application written in Kotlin and Java8 for authenticating user with Auth0 in Traefik.
Use the forward authentication configuration in Traefik and point it to this backend to protect frontends with Auth0 login.

The backend application supports multiple Auth0 applications and APIs based on the domainname/subdomainname of the
application and will save the JWT and the Access Token received from Auth0 as a cookie in the browser. When visitors 
access a protected frontend configured in Traefik, a http call will be sent to this backend to validate that the user is
a valid user.

# Pardon The Mess!
This software is in Alpha state right now beacuse its quite early in development. Expect missing features and bugs everywhere.
I'm working on making stuff much more stable like a Beta release, something that has basic features in place.

# Documentation
- [Overview](/docs/ovewview.md)
- [Development](/docs/development.md)
- [Installation](/docs/installation.md)
- [Configuration](/docs/configuration.md)
- [Auth0](/docs/auth0.md)
