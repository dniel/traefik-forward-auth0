# Upgrade Notes

## Upgrade to version 2.0 from version 1.0
#### Important note about opaque access tokens and audience.
There is some important breaking changes in version 2.0 of ForwardAuth. It is now mandatory to set an audience when requesting authorization. This change is required due to how Auth0 handles two different kinds of token formats, opaque tokens and jwt tokens, for access tokens. The only token that is possible to validate and verify is the jwt token. Therefor its from now on required to set the audience in the application config and the application will not work otherwise. See https://community.auth0.com/t/why-is-my-access-token-not-a-jwt/31028 for more info.

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
