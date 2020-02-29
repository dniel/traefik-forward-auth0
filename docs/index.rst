Welcome to ForwardAuth for Auth0's documentation!
=================================================

ForwardAuth for Auth0 is a authorization proxy written specifically
for use with the `Traefik`_, The Cloud Native Edge Router, and the `Auth0`_
Identity Management Platform.

`Traefik`_ will act as the gate to your applications, and the ForwardAuth
application will act as the gatekeeper and authorize requests to your
applications. The management of users, roles and permissions are handled
in Auth0.

Features
--------

-  Protect your applications with Authorization and Authentication
   using `Auth0`_ rich feature set.
-  Shared-host auth-mode for single sign-on for a whole domain and
   a whole set of services.
-  Sub-Path auth-mode for restricting single sign-on per sub-domain
   configuration to restrict SSO to a sub-domain.
-  Support for Auth0 API permissions natively to block access to
   services by API permissions.
-  Implement a powerful BeyondCorp policy control using Auth0 Rules +
   Auth0 Auth Core with RBAC.
-  Restrict selected HTTP methods, let other methods be unrestricted.
-  Signout and userinfo endpoint for other applications to use.


.. caution::
    There is some important breaking changes in version 2.0 of ForwardAuth.
    It is now mandatory to set an audience when requesting authorization.
    This change is required due to how Auth0 handles two different kinds of
    token formats, opaque tokens and jwt tokens, for access tokens. The only
    token that is possible to validate and verify is the jwt token. Therefor
    its from now on required to set the audience in the application config
    and the application will not work otherwise.

    The version 2.0 configuration also has some new fields that need to be
    set for the application to start up. See :doc:`Upgrade Notes <start/upgrade-notes>`
    for information about compatability and upgrades between versions. The page
    :doc:`Configuration <start/configuration>` should have a update to date
    example for the latest version.

Documentation
-------------

.. toctree:: :caption: Getting started
   :maxdepth: 2

   start/installation
   start/configuration
   start/live-demo
   start/upgrade-notes

.. toctree:: :caption: Examples
   :maxdepth: 2

   examples/kubernetes/index
   examples/traefik1/index
   examples/traefik2/index

.. toctree:: :caption: Auth0
   :maxdepth: 2

   auth0/auth0
   auth0/auth0-config
   auth0/oauth-oidc

.. toctree:: :caption: Developer guide
   :maxdepth: 2

   dev/development
   dev/api
   dev/diagrams
   dev/contributing

.. toctree:: :caption: Extra information
   :maxdepth: 2

   extra/receipts
   extra/faq
   extra/todos

.. _Traefik: https://containo.us/traefik/
.. _Auth0: https://www.auth0.com