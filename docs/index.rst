Welcome to ForwardAuth for Auth0's documentation!
=================================================

ForwardAuth for Auth0 is a authentication service that works that authorize and
authenticate http/https requests connected to a reverse proxy that use forward
authentication to an external service authorize requests. ForwardAuth is written
with the modern Traefik reverse proxy mainly and integrated specifically with
the Auht0 service.

Features
========

-  Centralized Auth-host mode for easy configuration when you have lots
   of applications.
-  Multiple-host auth mode for more advanced SSO per.
   sub-domain/applications configuration
-  Very flexible configuration
-  Support for Auth0 API permissions natively to block access to
   services by API permissions.
-  Implement a powerfull BeyondCorp policy control using Auth0 Rules +
   Auth0 Auth Core with RBAC.
-  Restrict selected HTTP methods, let other methods be unrestricted.
-  Signout and userinfo endpoint for other applications to use.

Update Notes
============

There is some important breaking changes in version 2.0 of ForwardAuth.
It is now mandatory to set an audience when requesting authorization.
This change is required due to how Auth0 handles two different kinds of
token formats, opaque tokens and jwt tokens, for access tokens. The only
token that is possible to validate and verify is the jwt token. Therefor
its from now on required to set the audience in the application config
and the application will not work otherwise.

The version 2.0 configuration also has some new fields that need to be
set for the application to start up. See the page `Upgrade Notes`_ for
information about compatability and upgrades between versions. The page
`Configuration`_ should have a update to date example for the latest
version.


Documentation
=============


.. toctree:: :caption: Getting started
   :maxdepth: 2

   installation
   configuration
   upgrade-notes

.. toctree:: :caption: Auth0
   :maxdepth: 2

   auth0
   oauth-oidc

.. toctree:: :caption: Developer guide
   :maxdepth: 2

   development
   overview
   contributing

.. toctree:: :caption: Extra information
   :maxdepth: 2

   receipts
   faq

Indices and tables
==================

* :ref:`genindex`
* :ref:`modindex`
* :ref:`search`

.. _Upgrade Notes: upgrade-notes.html
.. _Configuration: configuration.html
