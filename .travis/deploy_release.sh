#!/bin/bash
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
docker tag dniel/forwardauth dniel/forwardauth:$TRAVIS_TAG
docker tag dniel/forwardauth dniel/forwardauth:$BRANCH
docker tag dniel/forwardauth dniel/forwardauth:latest

# docker push dniel/forwardauth