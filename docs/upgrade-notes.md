# Upgrade Notes

## Upgrade to version 2.0 from version 1.0
#### Important note about opaque access tokens and audience.
From version 2.0 its mandatory to set an `audience` in the application.yaml.
The only way for ForwardAuth to validate that the Access Token has not been tampered with and has not expired is if you
use a token with JWT format See https://community.auth0.com/t/why-is-my-access-token-not-a-jwt/31028 for more info.

In effect it means that you now *must* create an API in Auth0 and set that API as audience in your application.yaml.

#### New *mandatory* fields in application.yaml to support new *userinfo* and *signout* feature.
```
userinfo-endpoint: https://xxxx.xx.auth0.com/userinfo
logout-endpoint:  https://xxxx.xx.auth0.com/v2/logout
```
Without them the application will not start.  

#### New *optional* config fields are available
```
  # after user has called /signout they will be redirected to this url
  return-to: http://example.com

  # if the user doesnt have all of the permissions required, he will get a 403 Permission Denied response.
  required-permissions:
    - read:whoami
    - read:website

```

See the [example configuration file](/example/application.yaml) for a complete example of an application.yaml file
that the FordwardAuth application need to run.
