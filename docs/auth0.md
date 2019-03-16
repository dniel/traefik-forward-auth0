# Auth0
The ForwardAuth-backend need to verify that the Access Token is a valid and authentic 
JWT from Auth0 and it check that the audience is the expected from the loaded config.

Quoted from [Auth0 documentation](https://auth0.com/docs/api-auth/tutorials/verify-access-token)
>If the Access Token you got from Auth0 is not a JWT but an opaque string 
>(like kPoPMRYrCEoYO6s5), this means that the access token was not issued 
>for your custom API as the audience. When requesting a token for your API, 
>make sure to use the audience parameter in the authorization or token request
>with the API identifier as the value of the parameter.

To make sure you always get a valid JWT Access Token, you could create an default API
add the audience in Auth0 Account Settings.

