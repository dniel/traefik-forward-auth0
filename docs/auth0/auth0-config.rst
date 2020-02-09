Auth0 Configuration
===================

Step by Step configuration
~~~~~~~~~~~~~~~~~~~~~~~~~~

* Go to https://auth0.com
* Sign in or register an account
* Note Tenant Domain provided by Auth0
* Login or create an account with https://github.com
* Go to Settings -> Developer Settings - OAuth Apps
* Create a new app (call it something to recognize it is linked to Auth0)
* Note the client Id and Secret
* Add homepage URL as https://<yourauth0accounthere>.auth0.com/
* Add authorization callback URL as https://<yourauth0accounthere>.auth0.com/login/callback
* Go back to Auth0
* Go to Connections -> Social
* Select Github and enter in your Github app ClientID and secret Credentials - NOTE: ENSURE Attribute "Email Address" is ticked
* Create an application on Auth0 (regular web app)
* Use the Auth0 clientID and Client Secret in your application.yaml file
  Make sure to specify POST method of token endpoint authentication (Drop down box)
  Enter in your Callback URL (https://<service>.<domain>/signin & https://<service>.<domain>/oauth/signin)
* Enter your origin URL (https://<your URL here>) and save changes
* Go to Users & Roles and Create a user with a real email address. You will use this later so remember it
* Click on Rules -> Whitelist
* Enter in your email address into the whitelist field (e.g. Line 8 "const whitelist = [ '<your email here>']; //authorized users")

.. _@gkoerk: https://github.com/gkoerk

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
