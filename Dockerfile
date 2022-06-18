FROM gcr.io/distroless/cc-debian11:nonroot
EXPOSE 8080

COPY --chown=$USER build/native/nativeCompile/forwardauth /app/forwardauth
CMD [ "/app/forwardauth" ]
