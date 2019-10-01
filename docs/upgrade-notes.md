# Upgrade Notes

## version 2.0
New mandatory fields in application.yaml to support new *userinfo* and *signout* feature.
* userinfo-endpoint: https://dniel.eu.auth0.com/userinfo
* logout-endpoint:  https://dniel.eu.auth0.com/v2/logout

Without them the application will not start.  
See the [example configuration file](/example/application.yaml) for a complete example of an application.yaml file
that the FordwardAuth application need to run.

## version 1.0