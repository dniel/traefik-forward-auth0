Configuration
=============

`@gkoerk`_ has create a good `step-by-step instructions`_ about
installing ForwardAuth and configuring Auth0.

Auth0 Configuration
-------------------

Suggestions of how to structure Applications, Apis and Permissions
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-  Add a common application, give it a name like for example Traefik.
-  Add a common Default API and set as Default Audience in your Auth0
   Tenant.
-  For a common set of services that the user should be able to navigate
   between without logging in again, create a shared Logical Api that
   span several services.
-  By default everyone has access that is able to authenticate, i.e,
   everyone with an account somewhere.

   -  Create permissions in your API.
   -  Enable RBAC and “Add Permissions to Access Token” under the API
      Settings.
   -  Add the permissions directly to users or to roles assigned to
      users.

-  `Represent Multiple APIs Using a Single Logical API in Auth0`_

Configure the Access Token to be a verifiable JWT
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The only way for ForwardAuth to validate that the Access Token has not
been tampered with and has not expired is if you use a token with JWT
format. This means that Auth0 *MUST* be configured with either and
Audience or a Default Audience in the Auth0 Tenant when requesting an
Access Token to receive a token of JWT Format, or else the user will get
Access Denied from ForwardAuth because the token could not be verified.

If someone authenticate and the Auth0 is configured to server a opaque
token, ForwardAuth will display an error page and will not let the user
in. - `Why is my access token no a JWT?`_

ForwardAuth example configuration
---------------------------------

See the `example configuration file`_ for a complete example of an
application.yaml file that the FordwardAuth application need to run.

.. _step-by-step instructions: https://github.com/gkoerk/QNAP-Docker-Swarm-Setup#forwardauth-setup-steps
.. _Represent Multiple APIs Using a Single Logical API in Auth0: https://auth0.com/docs/api-auth/tutorials/represent-multiple-apis
.. _Why is my access token no a JWT?: https://community.auth0.com/t/why-is-my-access-token-not-a-jwt/31028
.. _example configuration file: /_static/application.yaml
.. _@gkoerk: https://github.com/gkoerk