# Development
## Compile locally
`mvn clean install`

## Automated compilation with Travis-CI build system
The project has been comfigure to compile of Git push with [Travis-CI](https://travis-ci.com/dniel/traefik-forward-auth0)
automatically. When a build has sucessfully been compiled and packaged the resulting
Docker Image will be pushed to the [ForwardAuth DockerHub repository](https://hub.docker.com/r/dniel/forwardauth/) where it can be downloaded.

As a part of the automated build pipeline the code will be scanned with the static code analysis tool SonarCloud 
and [reports of the source code quality](https://sonarcloud.io/dashboard?id=dniel_traefik-forward-auth0) will be available.

Another tool that scan the code is Snyk.io which will 
[check dependencies in pom.xml](https://app.snyk.io/org/dniel/project/d49e200c-e638-4e45-b909-9bedc608c90d) for know vulnerabilities.


## Run
`mvn spring-boot:run` or start the main class `AuthApplication` from IDE

## Run with Docker
`docker run -v /config/application.yaml:/config/application.yaml -p 8080:8080 dniel/forwardauth`

## Run with Docker-Compose
`docker-compose up`

## Configuration
Put the application.yaml config somewhere where SpringBoot can find it. 
For example in a /config application directory.

Check out the `example` directory for example of an [application.yaml](/example/application.yaml) and a 
[traefik.toml](/example/traefik.toml) config for this application.

## Release
When a new release has been pushed to DockerHub, Spinnaker will find it and start the deployment pipeline.
The pipeline will update the internal development environment and my external site https://www.dniel.se 
also. The kubernetes configuration for the external site can be found at https://github.com/dniel/manifests/blob/master/forwardauth.yaml

## Tags
### Master
The master tag is the most stable. Before I merge to the master branch I run some manual tests on the feature branch to
and all unit tests should also be green. 

### Other tags
Each branch get its own tag with the same name when pushing to DockerHub.
Each commit get a tag with the commit timestamp and commit-shorthash.
Latest tag is only updated when pushing the master branch.

## Tech
- Java8
- Tomcat
- Kotlin
- JAX-RS/Jersey
- SpringBoot2
- Kubernetes
- Helm
- Docker
- Traefik
- Spockframework/Groovy (unit tests)

