FROM gcr.io/distroless/java17-debian11:nonroot
EXPOSE 8080

COPY --chown=$USER build/docker/main/layers/libs /application/libs
COPY --chown=$USER build/docker/main/layers/resources /application/resources
COPY --chown=$USER build/docker/main/layers/application.jar /application/application.jar

CMD [ "/application/application.jar" ]
