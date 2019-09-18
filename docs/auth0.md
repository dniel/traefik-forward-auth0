# Auth0

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

### Authentication
From [the Auth0 documentation on authentication](https://auth0.com/docs/application-auth/current) 
> Authentication refers to the process of confirming identity. While often used interchangeably with authorization, 
> authentication represents a fundamentally different function.
>
> In authentication, a user or application proves they are who they say they are by providing valid credentials 
> for verification. Authentication is often proved through a username and password, sometimes combined with other 
> elements called factors, which fall into three categories: what you know, what you have, or what you are.

### Authorization
From [the wikipedia article](https://en.wikipedia.org/wiki/Authorization) describing Authorization.
> Authorization is the function of specifying access rights/privileges to resources, which is related to information 
> security and computer security in general and to access control in particular. More formally, "to authorize" is to define an access policy.

The [Auth0 documentation](https://auth0.com/docs/authorization/concepts/authz-and-authn) describes the difference between 
Authentication and Authorization.

#### Auth0 Role Based Access Control, RBAC
The new system for [Auth0 RBAC](https://auth0.com/docs/authorization) is being released gradually during 2019 to replace 
the current Authorization Extension. 

#### Assign permissions to users
To use Auth0 RBAC for your API you need to go to the settings of the API and click on the enable switch. Then create 
under Permissions in your API's settings create permissions for your api to use. Then go to Users & Roles and add 
either directly to a user the permissions you created in your API, or create a role with a set of permissions and
assign to your users.

The RBAC system will run when the user log in and match all the scopes you send in to the permissions of the user.
Any permissions requested by the user that they dont have, will be removed by the RBAC system when returning the response.

#### Require Permissions to access an application in ForwardAuth
In the application.yaml file for ForwardAuth add a `required-permissions` to assign permissions that ForwardAuth
will check before letting the user access the application. The permissions is transferred on login from Auth0
to ForwardAuth using the Access Token when the Access Token is a JWT Token,  i.e, the audience for an API has been
set. This also means that permissions is only useful for access control to API's. There is no way to assign permissions
to applications in Auth0.

The required-permissions is an Array of permissions and if the user that tries to login and access an application
does not have the required permissions an HTTP 403 Forbidden will be thrown and an error page will be displayed.
E.g, 
```yaml
apps:
  - name: whoami.example.test
    audience: https://whoami.dniel.se
    required-permissions:
      - write:whoami
      - read:whoami
```

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

When ForwardAuth received a "Unautorized" error from Auth0 an HTTP 403 Forbidden will be thrown and an 
error page will be displayed to the user saying 403 Forbidden and the message from the rule will be displayed.

### Suggestions of how to structure Applications, Apis and Permissions
- Add one common application, maybe call it Traefik.
- Create API's for all applications you want Traefik and ForwardAuth to protect 
- Enable RBAC and Add Permissions to Access Token under API Settings. 
- Create permissions in your API.
- Add the permissions directly to users or to roles assigned to users.
- Use a Default Audience set on the tenant to always have a JWT token that can be verified. 

### Reference
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
