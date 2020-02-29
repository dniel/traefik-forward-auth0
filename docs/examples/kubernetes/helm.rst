.. _install-with-helm:

Install ForwardAuth with Helm
=============================

Quoted from the `Helm official website`_, Helm is described as
    Helm helps you manage Kubernetes applications — Helm Charts help you define, install,
    and upgrade even the most complex Kubernetes application.

    Charts are easy to create, version, share, and publish —
    so start using Helm and stop the copy-and-paste.


Helm Chart Documentation
------------------------

Read the `ForwardAuth Helm Chart`_ documentation for description of
the configuration options for the ForwardAuth Helm Chart.


Example values
--------------

See the `example values.yaml`_ for the chart to see how to specify values.
The example is from my running configuration of my development site.


Add the Helm repo
-----------------

.. code-block:: shell-session

   $ helm repo add dniel https://dniel.github.io/charts/


Install the chart
-----------------

When deploying the chart, it will do the following:

* Deploy ForwardAuth application
* Create configmap for application.yaml (application config file)
* Create secrets for default clientId and clientSecret
* Optionally, create Traefik 2 CRD Middleware
* Optionally, create Traefik 2 CRD IngressRoute for `auth.example.test`
* Optionally, create Traefik 2 CRD IngressRoute for `*.example.test/auth0`
* Optionally, create Ingress Object for `auth.example.test`
* Optionally, create Ingress Object for `*.example.test/auth0`

.. code-block:: shell-session

   $ helm install --name my-release forwardauth dniel/forwardauth -f values.yaml

Verify that the application has started
---------------------------------------

It should take about 30 seconds to start the application and reach READY state.
Verify the state by running

.. code-block:: shell-session

   $ kubectl get pods --selector="app=forwardauth" -n forwardauth
   NAME                           READY   STATUS    RESTARTS   AGE
   forwardauth-5878d8bd6d-qd4ql   1/1     Running   0          15d



.. _`ForwardAuth Helm Chart`: https://github.com/dniel/traefik-forward-auth0/tree/master/helm
.. _`example values.yaml`: https://github.com/dniel/manifests/blob/master/forwardauth-values.yaml
.. _`Helm official website`: https://www.helm.sh