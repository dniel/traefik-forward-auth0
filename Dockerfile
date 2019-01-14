FROM azul/zulu-openjdk-alpine:8

ADD /target/forwardauth.jar forwardauth.jar

EXPOSE 8080
ENV JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=1 -XshowSettings:vm"
ENTRYPOINT java $JAVA_OPTS -jar forwardauth.jar