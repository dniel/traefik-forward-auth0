# Auth0
The Auth0 service is conforming to the [OAuth 2 specification](https://tools.ietf.org/html/rfc6749)
and is a certified [Open ID Connect Provider](http://openid.net/certification/). It implements the 
[Open ID Connect specification.](http://openid.net/specs/openid-connect-core-1_0.html) and has added
its own features on top.

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

### API
The [Auth0 documentation on APIs](https://auth0.com/docs/apis) describes an API like this
>An API is an entity that represents an external resource, capable of accepting and responding to protected resource requests 
>made by applications. At the OAuth2 spec an API maps to the Resource Server.
>
>When an application wants to access an API's protected resources it must provide an Access Token. 
>The same Access Token can be used to access the API's resources without having to authenticate again, until it expires.

Register the web applications that you want to protect behind Traefik and ForwardAuth as APIs to be able to add
permissions to them.

### Application
From the [Auth0 documentation on Applications](https://auth0.com/docs/applications)
> Applications are primarily meant for human interaction, as opposed to APIs, which provide data to applications through 
> a standardized messaging system.
>
> The term application does not imply any particular implementation characteristics. For example, your application could 
> be a native app that executes on a mobile device, a single-page app that executes on a browser, or a regular web app
> that executes on a server.

Register Traefik ForwardAuth as an application to be able to assign APIs to it.

## Tokens
### Id Token
From the [Auth0 documentation](https://auth0.com/docs/api-auth/tutorials/adoption/api-tokens#access-vs-id-tokens) describing 
the difference between Access Tokens and Id tokens

>Note that the audience value (located in the aud claim) of the token is set to the application's identifier.
>This means that only this specific application should consume the token.   

>You can think of the ID Token as a performance optimization that allows applications to obtain user profile information
>without making additional requests after the completion of the authentication process. ID Tokens should never be used
>to obtain direct access to resources or to make authorization decisions.

Eg.
1) The ID token is only to be consumed by the ForwardAuth application that requested it, it shouldn't be read by other applications.
2) Don't limit access based on content in the ID Token. The Id token is for authenticating the user, not for authorization.

### Access Token
More from the same Auth0 documentation about [Access Tokens](https://auth0.com/docs/api-auth/tutorials/adoption/api-tokens#access-vs-id-tokens)

>The Access Token is meant to authorize the user to the API (resource server). As such, the token is Completely opaque 
>to applications -- applications should not care about the contents of the token.

>The token does not contain any information about the user except for the user ID (located in the sub claim). 
>The token only contains authorization information about the actions that application is allowed to perform at the API 
>(such permissions are referred to as scopes).

eg.
1) Use the content of the Access Token to make authorization decisions.
2) Use scopes to define permissions.
3) Traefik ForwardAuth application should'nt do anything with the Access Token.
4) The receiving API can do whatever it wants with the Access Token.

## Authentication
From [the Auth0 documentation on authentication](https://auth0.com/docs/application-auth/current) 
> Authentication refers to the process of confirming identity. While often used interchangeably with authorization, 
> authentication represents a fundamentally different function.
>
> In authentication, a user or application proves they are who they say they are by providing valid credentials 
> for verification. Authentication is often proved through a username and password, sometimes combined with other 
> elements called factors, which fall into three categories: what you know, what you have, or what you are.

## Authorization
From [the wikipedia article](https://en.wikipedia.org/wiki/Authorization) describing Authorization.
> Authorization is the function of specifying access rights/privileges to resources, which is related to information 
> security and computer security in general and to access control in particular. More formally, "to authorize" is to define an access policy.

The [Auth0 documentation](https://auth0.com/docs/authorization/concepts/authz-and-authn) describes the difference between 
Authentication and Authorization.

Auth0 has two systems for doing authorization to APIs. The current system is the Authorization Extension and will be 
gradually replaced by a more integrated solution in Auth0, Auth0 Role Based Access Control (RBAC). 
The Authorization Extension has currently (06.06.2019) more features but will be replaced eventually by Auth0 RBAC. 
Check out the [feature comparison](https://auth0.com/docs/authorization/concepts/core-vs-extension) for more details.

Both systems use a similar User, Roles and permissions model where you can assign permissions to roles or directly to a user.

Auth0 RBAC and Authorization Extension use APIs when assigning permissions. You can't assign a permission to an 
application. That means that to use authorization in you need to create an application for the ForwardAuth 
application and for all the applications you want ForwardAuth to protect you need to create an API.

Both systems can use [rules to decide access](https://auth0.com/docs/authorization/concepts/authz-rules).

### Auth0 Role Based Access Control, RBAC
The new system for [Auth0 RBAC](https://auth0.com/docs/authorization) is being released gradually during 2019 to replace 
the current Authorization Extension.    

To use Auth0 RBAC for your API you need to go to the settings of the API and click on the enable switch.
The RBAC system will run when the user log in and match all the scopes you send in to the permissions of the user.
Any permissions requested by the user that they dont have, will be removed by the RBAC system when returning the response.

### Rules
#### Example rule for access control with the Auth0 RBAC
Example rule that check if the audience is https://whoami.dniel.se, and if it is, it will authorize 
ccess if the user has an assigned admin role. or else throw a UnauthorizedError error to the user. 

For some reason I cant find any field in the rule context when running the rules that contains the resulting 
verified and filtered permissions list. The only info I have found about the RBAC in the context of a running rule is the
authorization object on the context which contain the assigned roles of the user. See the example rule below.

```javascript
function (user, context, callback) {
  var audience, scope = '';
  const assignedRoles = (context.authorization || {}).roles;
  audience = audience || (context.request && context.request.query && context.request.query.audience);  
  scope = scope || (context.request && context.request.query && context.request.query.scope);

  if(audience==='https://whoami.dniel.se' && 
     !assignedRoles.includes('admin')){
    return callback(new UnauthorizedError('The whoami app is only available to people in the admin group.'));
  }
  callback(null, user, context);
}
```


### Reference
- [OAuth 2 specification](https://tools.ietf.org/html/rfc6749)
- [Open ID Connect Provider](http://openid.net/certification/)
- [Open ID Connect specification.](http://openid.net/specs/openid-connect-core-1_0.html)
- [Auth0 documentation on APIs](https://auth0.com/docs/apis)
- [Auth0 documentation on Applications](https://auth0.com/docs/applications)
- [Auth0 documentation on difference between Access Tokens and ID Tokens](https://auth0.com/docs/api-auth/tutorials/adoption/api-tokens#access-vs-id-tokens)
- [Wikipedia article on Authorization](https://en.wikipedia.org/wiki/Authorization)
- [Auth0 documentation on authentication](https://auth0.com/docs/application-auth/current) 
- [Auth0 documentation on the difference between Authorization and Authentication](https://auth0.com/docs/authorization/concepts/authz-and-authn)
- [Auth0 documentation on implementing Authorization Rules](https://auth0.com/docs/authorization/concepts/authz-rules)
- [How to configure the Authorization Extension](https://auth0.com/docs/architecture-scenarios/spa-api/part-2#configure-the-authorization-extension)
- [Feature comparision between Auth0 RBAC and the Authorization Extension](https://auth0.com/docs/authorization/concepts/core-vs-extension)
- [Auth0 RBAC](https://auth0.com/docs/authorization)
- [Auth0 Rules to decide access](https://auth0.com/docs/authorization/concepts/authz-rules)