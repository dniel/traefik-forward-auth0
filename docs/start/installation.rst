Installation
============

Step by Step installation
-------------------------

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

TODO:* add list form gkoerk
