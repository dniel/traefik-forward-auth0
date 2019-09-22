# Configuration

## Auth0
[@gkoerk](https://github.com/gkoerk) has create a good [step-by-step instructions](https://github.com/gkoerk/QNAP-Docker-Swarm-Setup#forwardauth-setup-steps)
about installing ForwardAuth and configuring Auth0.

### Suggestions of how to structure Applications, Apis and Permissions
- Add a common application, give it a name like for example Traefik.
- Add a common Default API and set as Default Audience in your Auth0 Tenant.
- For a common set of services that the user should be able to navigate between without logging in again, create a 
shared Logical Api that span several services.
- By default everyone has access that is able to authenticate, i.e, everyone with an account somewhere.
  - Create permissions in your API.
  - Enable RBAC and "Add Permissions to Access Token" under the API Settings. 
  - Add the permissions directly to users or to roles assigned to users.
- [Represent Multiple APIs Using a Single Logical API in Auth0](https://auth0.com/docs/api-auth/tutorials/represent-multiple-apis)

### Configure the Access Token to be a verifiable JWT
The only way for ForwardAuth to validate that the Access Token has not been tampered with and has not expired is if you
use a token with JWT format. This means that Auth0 *MUST* be configured with either and Audience or a Default Audience 
in the Auth0 Tenant when requesting an Access Token to receive a token of JWT Format, or else the user will get Access
Denied from ForwardAuth because the token could not be verified. 

If someone authenticate and the Auth0 is configured to server a opaque token, ForwardAuth will display an error page
and will not let the user in.
- [Why is my access token no a JWT?](https://community.auth0.com/t/why-is-my-access-token-not-a-jwt/31028)


## ForwardAuth example configuration
*Note:*
* If no application in the apps-list is matched, the default application is used instead.
* If you leave out a property for an application in the apps-list in the config, the value in default will be used instead.

```yaml
domain: https://xxxxx.xx.auth0.com/
token-endpoint: https://xxx.xx.auth0.com/oauth/token
redirect-uri: http://www.example.test/oauth2/signin
authorize-url: https://xxxx.xx.auth0.com/authorize

default: 
    name: www.example.test
    client-id: <from auth0 application config>
    client-secret: <from auth0 application config>
    audience: <from auth0 api config>
    scope: "profile openid email"
    redirect-uri: http://www.example.test/oauth2/signin
    token-cookie-domain: example.test

apps:
  - name: www.example.test
    client-id: <from auth0 application config>
    client-secret: <from auth0 application config>
    audience: <from auth0 api config>
    scope: "profile openid email"
    redirect-uri: http://www.example.test/oauth2/signin
    token-cookie-domain: example.test

  - name: traefik.example.test          # this application will inherit most of the values from the default app.
    audience: <from auth0 api config>   # just the audience field will be used, all other values from the default.
```
