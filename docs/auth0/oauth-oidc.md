# OAuth and Open ID Connect
The Auth0 service is conforming to the [OAuth 2 specification](https://tools.ietf.org/html/rfc6749)
and is a certified [Open ID Connect Provider](http://openid.net/certification/). It implements the 
[Open ID Connect specification.](http://openid.net/specs/openid-connect-core-1_0.html) and has added
its own features on top.

Read more about https://developer.okta.com/blog/2017/06/21/what-the-heck-is-oauth

## The different roles of OAuth and how they are related to ForwardAuth
From Section 1.1 in the [OAuth specification](https://tools.ietf.org/html/rfc6749#section-1.1)
>   OAuth defines four roles:
>
>   resource owner
>      An entity capable of granting access to a protected resource.
>      When the resource owner is a person, it is referred to as an
>      end-user.
>
>   resource server
>      The server hosting the protected resources, capable of accepting
>      and responding to protected resource requests using access tokens.
>
>   client
>      An application making protected resource requests on behalf of the
>      resource owner and with its authorization.  The term "client" does
>      not imply any particular implementation characteristics (e.g.,
>      whether the application executes on a server, a desktop, or other
>      devices).
>
>   authorization server
>      The server issuing access tokens to the client after successfully
>      authenticating the resource owner and obtaining authorization.
 
In the Auth0 and ForwardAuth setup with Traefik
- _Auth0_ is the authorization server
- _Client_ is the ForwardAuth application
- _Resource Server_ is a web application you want to protect with Traefik.

In the Auth0 configuration
- _Client_ is an Application.
- _Resource Server_ is an API.

## Tokens
The ForwardAuth make heavy use of the Access Tokens and ID Tokens. Its actually what the application 
is all about, it retrieves, validates, verifies, parses and checks the Access Tokens and the ID tokens and its content
to make sure that the user has access to the URL he/she requested. The two tokens come from two different specifications
and serves different purposes.

* The ID Token comes from the OIDC spec, and is for authentication, eg. the user profile
* The Access Token comes from the OAuth2 spec, and is for authorization, eg. the user access assertions.
* There is also a Refresh token also from the OAuth2 spec, but its not in use in the ForwardAuth application. 

### Id Token
From the [Auth0 documentation](https://auth0.com/docs/api-auth/tutorials/adoption/api-tokens#access-vs-id-tokens) describing 
the difference between Access Tokens and Id tokens

>Note that the audience value (located in the aud claim) of the token is set to the application's identifier.
>This means that only this specific application should consume the token.   

>You can think of the ID Token as a performance optimization that allows applications to obtain user profile information
>without making additional requests after the completion of the authentication process. ID Tokens should never be used
>to obtain direct access to resources or to make authorization decisions.

I.e,
1) The ID token is only to be consumed by the ForwardAuth application that requested it, it shouldn't be read by other applications.
2) Don't limit access based on content in the ID Token. The Id token is for transferring the user profile, not for authorization.

### Access Token
More from the same Auth0 documentation about [Access Tokens](https://auth0.com/docs/api-auth/tutorials/adoption/api-tokens#access-vs-id-tokens)

>The Access Token is meant to authorize the user to the API (resource server). As such, the token is Completely opaque 
>to applications -- applications should not care about the contents of the token.

>The token does not contain any information about the user except for the user ID (located in the sub claim). 
>The token only contains authorization information about the actions that application is allowed to perform at the API 
>(such permissions are referred to as scopes).

I.e,
1) Use the content of the Access Token to make authorization decisions.
2) Use scopes to define permissions.
3) Traefik ForwardAuth application shouldn't do anything with the Access Token.
4) The receiving API can do whatever it wants with the Access Token.

## Reference
- [OAuth 2 specification](https://tools.ietf.org/html/rfc6749)
- [Open ID Connect Provider](http://openid.net/certification/)
- [Open ID Connect specification.](http://openid.net/specs/openid-connect-core-1_0.html)
- [Auth0 documentation on difference between Access Tokens and ID Tokens](https://auth0.com/docs/api-auth/tutorials/adoption/api-tokens#access-vs-id-tokens)