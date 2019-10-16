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

# Next version of ForwardAuth, aka. ForwardAuth 2.0
Right now I'm finishing a major refactoring and improved version of ForwardAuth that will get the version 2.0 when released.
It is now ready for testing and available at dockerhub with the tag 2.0-rc1. I think this is the best and most stable version
of the application and Im trying to iron out eventual bugs found.   
- Please test the new version and create issues for bugs you find.  
- Source Code for the new version: https://github.com/dniel/traefik-forward-auth0/tree/2.0-rc1
- Dockerhub image url: index.docker.io/dniel/forwardauth:2.0-rc1
- Upgrade notes for v1.0 user: https://github.com/dniel/traefik-forward-auth0/blob/2.0-rc1/docs/upgrade-notes.md

# Documentation
- [Overview](/docs/overview.md)
- [Development](/docs/development.md)
- [Installation](/docs/installation.md)
- [Configuration](/docs/configuration.md)
- [Auth0](/docs/auth0.md)
- [Contributing](/docs/contributing.md)
