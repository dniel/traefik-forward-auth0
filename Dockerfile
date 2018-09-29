FROM anapsix/alpine-java:8

ADD /target/forwardauth.jar forwardauth.jar

EXPOSE 8080
CMD ["java", "-jar", "forwardauth.jar"]