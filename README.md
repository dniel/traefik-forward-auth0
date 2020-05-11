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

# Update Notes
There is some important breaking changes in version 2.0 of ForwardAuth. 
It is now mandatory to set an audience when requesting authorization. This change is required due to 
how Auth0 handles two different kinds of token formats, opaque tokens and jwt tokens, for access tokens.
The only token that is possible to validate and verify is the jwt token. Therefor its from now on 
required to set the audience in the application config and the application will not work otherwise.  

The version 2.0 configuration also has some new fields that need to be set for the application to start up.
See the page [Upgrade Notes](https://traefik-forward-auth0.readthedocs.io/en/latest/start/upgrade-notes.html) for information about compatability and upgrades between versions.
The [Configuration](https://traefik-forward-auth0.readthedocs.io/en/latest/start/configuration.html) page  should have a update to date example for the latest version.

*For those that want to delay upgrade from 1.0 to 2.0 version, there is a docker image that has been tagged 1.0
that you can continue to use, but it will not get any further updates and I encourage you to upgrade to 2.0 as
soon as possible.*

# Features
- Centralized Auth-host mode for easy configuration when you have lots of applications.
- Multiple-host auth mode for more advanced SSO per. sub-domain/applications configuration
- Very flexible configuration
- Support for Auth0 API permissions natively to block access to services by API permissions.
- Implement a powerfull BeyondCorp policy control using Auth0 Rules + Auth0 Auth Core with RBAC.
- Restrict selected HTTP methods, let other methods be unrestricted.
- Signout and userinfo endpoint for other applications to use.

# Documentation
Checkout the documentation at [Read The Docs](https://traefik-forward-auth0.readthedocs.io/en/latest/)
