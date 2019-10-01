# Upgrade Notes

## version 2.0
New *mandatory* fields in application.yaml to support new *userinfo* and *signout* feature.
* userinfo-endpoint: https://xxxx.xx.auth0.com/userinfo
* logout-endpoint:  https://xxxx.xx.auth0.com/v2/logout

Without them the application will not start.  
Also for applications, new *optional* config fields are available
* required-permissions: []
* return-to: http://example.com/signout.html

See the [example configuration file](/example/application.yaml) for a complete example of an application.yaml file
that the FordwardAuth application need to run.

## version 1.0