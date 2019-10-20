# Receipts

### Automatic assign default roles based on conditions in rule with the new [Auth0's Core Authorization](https://auth0.com/docs/authorization/guides/how-to)
Its nice to be able to give the users default roles on Signup. Its possible by using the management API
from Auth0 in a Rule to do custom roles logic on signup.

Found in Auth0 Community forum
* https://community.auth0.com/t/how-do-i-add-a-default-role-to-a-new-user-on-first-login/25857
* https://community.auth0.com/t/add-role-to-a-user-on-sign-up-using-roles-not-app-metadata/26321/4

```
function (user, context, callback) {
  // Roles should only be set to verified users.
  if (!user.email || !user.email_verified) {
    return callback(null, user, context);
  }
  
  // short-circuit if the user signed up already or is using a refresh token
  if (context.stats.loginsCount > 1 || context.protocol === 'oauth2-refresh-token') {
    return callback(null, user, context);
  }
  
  const getRolesForUser = (user) => {
    const roles = [
      '<default role id assign to all users on signup eg. something like "rol_?????">' // default
    ];
    try {
      const emailDomain = user.email.split('@')[1].toLowerCase();
      switch (emailDomain) {
        case 'example.com':
          roles.push('<default role id assign to users from example.com domain on signup eg. something like "rol_?????">');
          break;
        // More custom rules here
      }
      return roles;
    } catch (e) {
      console.error(e);
      return [];
    }
  };
  
  const roles = getRolesForUser(user);
  var ManagementClient = require('auth0@2.17.0').ManagementClient;
  var management = new ManagementClient({
    token: auth0.accessToken,
    domain: auth0.domain
  });
  
  // Update the user's roles
  management.assignRolestoUser({ id : user.user_id }, { roles: roles }, (err) => {
    if (!err) {
      console.log('Roles [' + roles.join(', ') + '] assigned to user [' + user.email + ']');
    } else {
      console.error(err);
    }
    return callback(err, user, context);
  });
  
}
```
