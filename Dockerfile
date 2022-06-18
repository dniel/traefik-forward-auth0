FROM gcr.io/distroless/base:debug-nonroot
EXPOSE 8080

COPY build/native/nativeCompile/forwardauth /app/forwardauth
CMD [ "/app/forwardauth" ]
