# Configuration

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
