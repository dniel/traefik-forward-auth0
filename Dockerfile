FROM gcr.io/distroless/java17-debian11:nonroot
ENV USER=appuser
ENV GROUP=$USER \
    HOME=/home/$USER

RUN addgroup -S $GROUP && \
    adduser -S $USER -G $GROUP && \
    chown -R $USER $HOME

WORKDIR $HOME

EXPOSE 8080

COPY --chown=$USER Docker/runapp.sh $HOME/
RUN chmod 755 $HOME/*.sh

COPY --chown=$USER build/docker/main/layers/libs $HOME/application/libs
COPY --chown=$USER build/docker/main/layers/resources $HOME/application/resources
COPY --chown=$USER build/docker/main/layers/application.jar $HOME/application/application.jar

CMD [ "./runapp.sh" ]
