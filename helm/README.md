# ForwardAuth Helm Chart
ForwardAuth for Auth0 is a authorization proxy written specifically
for use with the `Traefik`_, The Cloud Native Edge Router, and the `Auth0`_
Identity Management Platform.

`Traefik`_ will act as the gate to your applications, and the ForwardAuth
application will act as the gatekeeper and authorize requests to your
applications. The management of users, roles and permissions are handled
in Auth0.

## Prerequisites Details
* Kubernetes 1??+

## Chart Details
This chart will do the following:

* Deploy ForwardAuth application
* Create configmap for application.yaml (application config file)
* Create secrets for default clientId and clientSecret
* Optionally, create Traefik 2 CRD Middleware
* Optionally, create Traefik 2 CRD IngressRoute for auth.example.test
* Optionally, create Traefik 2 CRD IngressRoute for *.example.test/auth0
* Optionally, create Ingress Object for auth.example.test
* Optionally, create Ingress Object for *.example.test/auth0

## Installing the Chart

To install the chart with the release name `my-release`:

```bash
$ helm install --name my-release dniel/forwardauth
```

## Configuration

The following table lists the configurable parameters of the consul chart and their default values.

| Parameter                        | Description                           | Default                                                    |
| -------------------------------- | ------------------------------------- | ---------------------------------------------------------- |
| `Image`                          | Container image name                  | `dniel/forwardauth`                                        |
| `ImageTag`                       | Container image tag                   | `latest`                                                   |
| `ImagePullPolicy`                | Container pull policy                 | `Always`                                                   |
| `logLevel`                       | application loglevel                  | `DEBUG`                                                    |
| `domain`                         | Auth0 tenant domain                   | ` `                                                        |
| `tokenEndpoint`                  | Auth0 token endpoint                  | ` `                                                        |
| `default.name`                   | default app name                      | `example.test`                                             |
| `default.clientid`               | default clientid from Auth0 App       | `<from auth0 config>`                                      |
| `default.clientsecret`           | default clientsecret from Auth0 App.  | `<from auth0 config>`                                      |
| `default.audience`               | default Auth0 API audience            | `<from auth0 api>`                                         |
| `default.scope`                  | default scopes to reques              | `profile openid email`                                     |
| `default.redirectUri`            | default redirect url for signin       | `http://auth.example.test/signin`                          |
| `default.tokenCookieDomain`      | default cookie domain                 | `example.test`                                             |
| `mode.host`                      | enable/disable host mode              | `false`                                                    |
| `mode.path`                      | enable/disable path mode              | `false`                                                    |
| `ingressroute.enabled`           | generate Traefik CRD IngressRoute     | `false`                                                    |
| `ingressroute.hostname`          | hostname for IngressRoute             | `example.test`                                             |
| `ingressroute.path`              | path for IngressRoute                 | `/auth`                                                    |
| `ingressroute.certResolver`      | certificate resolver to use           | `default`                                                  |
| `middleware.enabled`             | generate Traefik CRD Middleware       | `false`                                                    |
| `middleware.trustForwardHeader`  | enable/disable trustForwardHeader     | `true`                                                     |
| `middleware.authResponseHeaders` | headers to add from ForwardAuth resp. | `[authorization,x-forwardauth-name,x-forwardauth-sub,x-forwardauth-email]` |
| `ingress.enabled`                | enable/disable Ingress                | `false`                                                    |
| `ingress.hostname`               | hostname for Ingress                  | `auth.example.test`                                        |
| `ingress.annotations`            | annotations to add to Ingress         | `[]`                                                       |
| `ingress.path`                   | path to add for Ingress               | `/auth`                                                    |
| `applicationYaml`                | Application config for configmap      |                                                            |
| `resources.limits.memory`        |                                       | `512Mi`                                                    |
| `resources.requests.memory`      |                                       | `512Mi`                                                    |
| `nodeSelector`                   |                                       | `{}`                                                       |
| `tolerations`                    |                                       | `[]`                                                       |
| `affinity`                       |                                       | `{}`                                                       |


Specify each parameter using the `--set key=value[,key=value]` argument to `helm install`.

Alternatively, a YAML file that specifies the values for the parameters can be provided while installing the chart. For example,

```bash
$ helm install --name my-release -f values.yaml dniel/forwardauth
```
> **Tip**: You can use the default [values.yaml](values.yaml)
