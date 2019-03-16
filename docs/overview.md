# Overview

## Diagrams
### Component diagram
![UML component diagram](/docs/component.png "Component diagram")

### Sequence diagram
![UML sequence diagram](/docs/sequence.png "Sequence diagram")


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

