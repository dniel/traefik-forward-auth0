# Installation

## Traefik
ForwardAuth is meant to provide integration between Auth0 and Traefik using forward authentication in
Traefik. Have a look at the [Traefik configuration for forward authentication](https://docs.traefik.io/configuration/entrypoints/#forward-authentication)
and also the [Kubernetes configuration for authentication with annotations](https://docs.traefik.io/configuration/backends/kubernetes/#annotations) in Traefik.

## ForwardAuth
ForwardAuth need an [application.yaml config file](/example/application.yaml) with application configuration to run.
See the [values.yaml](/helm/values.yaml) file in the Helm chart on how to generate the application config as a configmap in kubernetes.
Also read the [configuration section in the docs](/docs/configuration.md) for how to configure the application.

## Kubernetes
Check out the [Helm chart](https://github.com/dniel/traefik-forward-auth0/tree/master/helm)  to create Kubernetes deployment configuration.

## Ingress objects
After deploying the application and its configuration you need to configure all the application that you want to 
authenticate using ForwardAuth using annotations on the Ingress object in Kubernetes. For an example of how to do 
this have a look at [the manifest for the example application whoami](https://github.com/dniel/manifests/blob/master/whoami.yaml#L64-L86)