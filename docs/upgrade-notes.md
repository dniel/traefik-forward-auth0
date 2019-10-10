# Upgrade Notes

## Upgrade to version 2.0 from version 1.0
New *mandatory* fields in application.yaml to support new *userinfo* and *signout* feature.
```
userinfo-endpoint: https://xxxx.xx.auth0.com/userinfo
logout-endpoint:  https://xxxx.xx.auth0.com/v2/logout
```

Without them the application will not start.  
Also for applications, new *optional* config fields are available
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
