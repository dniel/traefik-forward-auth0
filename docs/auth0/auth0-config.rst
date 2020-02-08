Auth0 Configuration
-------------------

Suggestions of how to structure Applications, Apis and Permissions
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

-  Add a common application, give it a name like for example Traefik.
-  Add a common Default API and set as Default Audience in your Auth0
   Tenant.
-  `Represent Multiple APIs Using a Single Logical API in Auth0`_ that
   span several services to use a single sign in on multiple APIs.
-  By default everyone has access that is able to authenticate, i.e,
   everyone with an account in one of the enabled Auth0 Connections.

   -  Create permissions in your API.
   -  Enable RBAC and “Add Permissions to Access Token” under the API
      Settings.
   -  Add the permissions directly to users or to roles assigned to
      users.


Configure the Access Token to be a verifiable JWT
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The only way for ForwardAuth to validate that the Access Token has not
been tampered with and has not expired is if you use a token with JWT
format. This means that Auth0 *MUST* be configured with either and
Audience or a Default Audience in the Auth0 Tenant when requesting an
Access Token to receive a token of JWT Format, or else the user will get
Access Denied from ForwardAuth because the token could not be verified.

.. warning::
    If someone authenticate and the Auth0 is configured to serve a opaque
    token, ForwardAuth will display an error page and will not let the user
    enter the requested application. See `Why is my access token no a JWT?`_
    for more info.

.. _Why is my access token no a JWT?: https://community.auth0.com/t/why-is-my-access-token-not-a-jwt/31028
.. _Represent Multiple APIs Using a Single Logical API in Auth0: https://auth0.com/docs/api-auth/tutorials/represent-multiple-apis
