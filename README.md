# Traefik Forward Auth0
This is a SpringBoot backend application written i Kotlin and Java8 for authenticating user with Auth0 in Traefik.
Use the forward authentication configuration in Traefik and point it to this backend to protect frontends with Auth0 login.

The backend application supports multiple Auth0 applications and APIs based on the domainname/subdomainname of the
application and will save the JWT and the Access Token received from Auth0 as a cookie in the browser. When visitors 
access a protected frontend configured in Traefik, a http call will be sent to this backend to validate that the user is
a valid user.


# Development
## Compile
`mvn clean install`

## Run
`mvn spring-boot:run` or start the main class `AuthApplication` from IDE


## Configuration
Put the application config somewhere where SpringBoot can find it. 
For example in a /config application directory.

### Example config
```yaml
domain: https://xxxxx.xx.auth0.com/
token-endpoint: https://xxx.xx.auth0.com/oauth/token
redirect-uri: http://www.example.test/oauth2/signin
authorize-url: https://xxxx.xx.auth0.com/authorize

default:
    name: www.example.test
    client-id: <from auth0 application config>
    client-secret: <from auth0 application config>
    audience: <from auth0 api config> or blank
    scope: "profile openid email"
    redirect-uri: http://www.example.test/oauth2/signin
    token-cookie-domain: example.test

apps:
  - name: www.example.test
    client-id: <from auth0 application config>
    client-secret: <from auth0 application config>
    audience: <from auth0 api config> or blank
    scope: "profile openid email"
    redirect-uri: http://www.example.test/oauth2/signin
    token-cookie-domain: example.test

  - name: traefik.example.test
    client-id: <from auth0 application config>
    client-secret: <from auth0 application config>
    audience: <from auth0 api config> or blank
    scope: "profile openid email"
    redirect-uri: http://traefik.example.test/oauth2/signin
    token-cookie-domain: traefik.example.test
```

# Publishing
I am publishing my Docker images to https://hub.docker.com/r/dniel/forwardauth
If you want to build your own images, use the docker CLI to build and publish images to your own repo instead.

## Deployment to Kubernetes
Check out the helm chart directory `helm` for template for the Helm chart to create Kubernetes deployment configuration.

# Tech
- Java8
- Tomcat
- Kotlin
- JAX-RS
- Kubernetes
- Helm
- Docker

# TODO
- create unit tests
- create integration tests
- signout endpoint 
- user profile endpoint
- error handling, the current code is not handling much of Auth0 errormessages and does not format errors to the users.
- fix the helm chart so that it will create a valid config without need to generate yaml and manually edit the result to deploy it.
