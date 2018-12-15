# Traefik Forward Auth0
This is a SpringBoot backend application written in Kotlin and Java8 for authenticating user with Auth0 in Traefik.
Use the forward authentication configuration in Traefik and point it to this backend to protect frontends with Auth0 login.

The backend application supports multiple Auth0 applications and APIs based on the domainname/subdomainname of the
application and will save the JWT and the Access Token received from Auth0 as a cookie in the browser. When visitors 
access a protected frontend configured in Traefik, a http call will be sent to this backend to validate that the user is
a valid user.

# Pardon The Mess!
This software is in Alpha state right now beacuse its quite early in development. Expect missing features and bugs everywhere.
I'm working on making stuff much more stable like a Beta release, something that has basic features in place.

# Development
## Compile
`mvn clean install`

## Run
`mvn spring-boot:run` or start the main class `AuthApplication` from IDE

## Configuration
Put the application.yaml config somewhere where SpringBoot can find it. 
For example in a /config application directory.

Check out the `example` directory for example of an application.yaml and a traefik.toml config for this application.

### Auth0 configuration
Add Applications to your Auth0 domain. 
The ForwardAuth-backend need to verify that the Acces Token is a valid and authentic 
JWT from Auth0 and it check that the audience is the expected from the loaded config.

Quoted from [Auth0 documentation](https://auth0.com/docs/api-auth/tutorials/verify-access-token)
>If the Access Token you got from Auth0 is not a JWT but an opaque string 
>(like kPoPMRYrCEoYO6s5), this means that the access token was not issued 
>for your custom API as the audience. When requesting a token for your API, 
>make sure to use the audience parameter in the authorization or token request
>with the API identifier as the value of the parameter.

To make sure you always get a valid JWT Access Token, you could create an default API
add the audience in Auth0 Account Settings.

### ForwardAuth-backend example configuration
When a request is received by the ForwardAuth-backend and it need to authenicate the use, it uses the x-forwarded-host 
to match a application name to the loaded list of apps in the config. If an application is matched the backend uses the
client-id, client-secret, scope and audience for that application to request a access token exchange code from Auth0.

If no application in the apps list is matched, the default application is used instead.

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

## Release
My Jenkins server has been configured to automatically poll for source code changes in a multi-branch pipeline. 
New branches will be automatically found and built by Jenkins and when successfully compiled and packaged 
docker images will be pushed to the https://hub.docker.com/r/dniel/forwardauth/ repository.
The images will be tagged with sourcecode commit id, timestamp and branch name.

When a new release has been pushed to dockerhub Spinnaker will find it and start the deployment pipeline.
The pipeline will update the internal development environment and my external site https://www.dniel.se 
also. The kubernetes configuration for the external site can be found at https://github.com/dniel/manifests/blob/master/forwardauth.yaml

## Deployment to Kubernetes
Check out the https://github.com/dniel/traefik-forward-auth0/tree/master/helm directory for the Helm chart to create Kubernetes deployment configuration.

# Tech
- Java8
- Tomcat
- Kotlin
- JAX-RS
- Kubernetes
- Helm
- Docker
- Traefik

# TODO
- create unit tests
- create integration tests
- signout endpoint 
- user profile endpoint
- error handling, the current code is not handling much of Auth0 errormessages and does not format errors to the users.
- fix the helm chart so that it will create a valid config without need to generate yaml and manually edit the result to deploy it.
