# Overview

## Diagrams
### Component diagram
![UML component diagram](/docs/component.png "Component diagram")

### Sequence diagram
![UML sequence diagram](/docs/sequence.png "Sequence diagram")

### Authorization and Authentication activity diagram
![UML activity diagram](http://www.plantuml.com/plantuml/png/XPFHIiGm44MVxLVq1_m2GJOsGtUmJNLcue8lOM551HLProVntqsM5T99j8-TU-xqzZPPVHos-yFdwqkpUtxSRz-VshxzQixkp83M2Be-e0qoNODhpozRqtN077eoJi1U_CcXIy9fCCUE8qvGDZpj_SMDb_L4U1-b10BHHFEnyXFSUc016pmDaQojNewcqKnYCq9WVyKfniQW11KcUun8pQastW2Do8oJutyCPThwDwlbcpKA35XlH8bB_rNZkdaMmYw98yGAbBcGbzvMDHJ79tcgLIgwR4xnMHmB3ezfPfqdm07OG62He1CDL3PyDwk7jrsyKpy0 "UML activity diagram")

## API Endpoints
The ForwardAuth-backend exposes by default the following application endpoints on the port 8080. 
### Authorize
Return 200 OK if user is authorized to access the requested URL, this is the endpoint used by Traefik to 
decide to let the request through to the target website or deny access. If denied access it will redirect to
the authorization url at Auth0 to perform authorization. Will verify that the access token and id-token set in
browser session is valid.

### Signin
The callback URL that Auth0 redirects after the user has authorized the requst and signed in.
Will set the Session Cookies in the browser with Access Token and ID-Token to hold the current user session 
between http request.

### Signout
Call this endpoint when logged in to remove the session cookies from your browser and call Auth0 logout endpoint
which will log out your session in Auth0 as well.

### Userinfo
Call this endpoint when logged in to retrieve user information.
