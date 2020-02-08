Upgrade Notes
=============

Upgrade to version 2.0 from version 1.0
---------------------------------------

.. caution:: Important note about opaque access tokens and audience.

    There is some important breaking changes in version 2.0 of ForwardAuth.
    It is now mandatory to set an audience when requesting authorization.
    This change is required due to how Auth0 handles two different kinds of
    token formats, opaque tokens and jwt tokens, for access tokens. The only
    token that is possible to validate and verify is the jwt token. Therefor
    its from now on required to set the audience in the application config
    and the application will not work otherwise.

    In effect it means that you now *must* create an API in Auth0 and set
    that API as audience in your application.yaml.

    See `Why is my access token not a jwt?`_ for more info.

New *mandatory* fields in application.yaml to support new *userinfo* and *signout* feature.
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. literalinclude:: ../../example/application.yaml
   :language: yaml
   :lines: 15-16

New *optional* config fields are available
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. literalinclude:: ../../example/application.yaml
   :language: yaml
   :lines: 89-92

See the :doc:`example configuration </examples/forwardauth>` for a
complete example of an application.yaml file that the FordwardAuth
application need to run.

.. _Why is my access token not a jwt?: https://community.auth0.com/t/why-is-my-access-token-not-a-jwt/31028