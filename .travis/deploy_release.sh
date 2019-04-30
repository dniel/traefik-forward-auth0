#!/bin/bash
echo "$DOCKER_PASSWD" | docker login -u "$DOCKER_USER" --password-stdin
docker tag dniel/forwardauth dniel/forwardauth:$TRAVIS_TAG
docker tag dniel/forwardauth dniel/forwardauth:$BRANCH
docker tag dniel/forwardauth dniel/forwardauth:latest

# docker push dniel/forwardauth