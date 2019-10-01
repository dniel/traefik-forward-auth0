[![Known Vulnerabilities](https://snyk.io/test/github/dniel/traefik-forward-auth0/badge.svg)](https://snyk.io/test/github/dniel/traefik-forward-auth0)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=dniel_traefik-forward-auth0&metric=alert_status)](https://sonarcloud.io/dashboard?id=dniel_traefik-forward-auth0)
[![Build Status](https://travis-ci.com/dniel/traefik-forward-auth0.svg?branch=master)](https://travis-ci.com/dniel/traefik-forward-auth0)


# Auth0 ForwardAuth for Traefik
This is a SpringBoot backend application written in Kotlin and Java8 for authenticating user with Auth0 in Traefik.
Use the forward authentication configuration in Traefik and point it to this backend to protect frontends with Auth0 login.

The backend application supports multiple Auth0 applications and APIs based on the domainname/subdomainname of the
application and will save the JWT and the Access Token received from Auth0 as a cookie in the browser. When visitors 
access a protected frontend configured in Traefik, a http call will be sent to this backend to validate that the user is
a valid user.

# Features
- Centralized auth mode for simple config via a shared authorization service.
- Multiple-domain auth mode for more advanced multiple-domain/services configuration
- Very flexible configuration
- Support for Auth0 API permissions natively to block access to services by API permissions.
- Restrict selected HTTP methods, let other methods be unrestricted.
- Signout and userinfo endpoint for other applications to use.

# Documentation
- [Overview](/docs/overview.md)
- [Development](/docs/development.md)
- [Installation](/docs/installation.md)
- [Upgrade Notes](/docs/upgrade-notes.md)
- [Configuration](/docs/configuration.md)
- [Auth0](/docs/auth0.md)
- [OAuth and OpenID Connect](/docs/oauth-oidc.md)
- [FAQ](/docs/faq.md)
- [Contributing](/docs/contributing.md)
