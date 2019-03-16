# Development
## Compile
`mvn clean install`

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
My Jenkins server has been configured to automatically poll for source code changes in a multi-branch pipeline. 
New branches will be automatically found and built by Jenkins and when successfully compiled and packaged 
docker images will be pushed to the [ForwardAuth Dockerhub repository](https://hub.docker.com/r/dniel/forwardauth).
The images will be tagged with sourcecode commit id, timestamp and branch name.

When a new release has been pushed to dockerhub Spinnaker will find it and start the deployment pipeline.
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

