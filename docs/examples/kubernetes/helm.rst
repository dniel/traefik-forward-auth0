.. _install-with-helm:

Install ForwardAuth in Kubernetes with Helm chart
=================================================

.. note::

    `Helm`_ is an easy way of deploying application to Kubernetes.
    When deploying the chart, it will do the following:

    * Deploy ForwardAuth application
    * Create configmap for application.yaml (application config file)
    * Create secrets for default clientId and clientSecret
    * Optionally, create Traefik 2 CRD Middleware
    * Optionally, create Traefik 2 CRD IngressRoute for auth.example.test
    * Optionally, create Traefik 2 CRD IngressRoute for *.example.test/auth0
    * Optionally, create Ingress Object for auth.example.test
    * Optionally, create Ingress Object for *.example.test/auth0


Check out the Helm Chart README
-------------------------------

Also see the `ForwardAuth Helm Chart`_ code and documentation for description of
what is configration options that is available.


Check out the configuration for the Helm chart
----------------------------------------------

Checkout my `example values.yaml`_ for chart to see how to specify values.
The example is from my running configuration of my development site.


Add the Helm repo
-----------------

.. code-block:: shell-session

   $ helm repo add dniel https://dniel.github.io/charts/


Install chart with
------------------

.. code-block:: shell-session

   $ helm install --name my-release forwardauth dniel/forwardauth -f values.yaml


.. _`ForwardAuth Helm Chart`: https://github.com/dniel/traefik-forward-auth0/tree/master/helm
.. _`example values.yaml`: https://github.com/dniel/manifests/blob/master/forwardauth-values.yaml
.. _`Helm`: https://www.helm.io