FROM gcr.io/distroless/java17-debian11:nonroot
EXPOSE 8080

COPY --chown=$USER Docker/runapp.sh $HOME/
COPY --chown=$USER build/docker/main/layers/libs $HOME/application/libs
COPY --chown=$USER build/docker/main/layers/resources $HOME/application/resources
COPY --chown=$USER build/docker/main/layers/application.jar $HOME/application/application.jar

CMD [ "./runapp.sh" ]
