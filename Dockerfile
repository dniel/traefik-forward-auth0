FROM gcr.io/distroless/base:nonroot
EXPOSE 8080

COPY build/native/nativeCompile/forwardauth /app/forwardauth
CMD [ "/app/forwardauth" ]
