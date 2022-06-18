FROM gcr.io/distroless/base
EXPOSE 8080

COPY build/native/nativeCompile/forwardauth /app/forwardauth
CMD [ "/app/forwardauth" ]
