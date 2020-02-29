Run in Docker Compose
=====================

Docker Compose is a nice way of testing the Traefik configuration
locally before deploying it to Kubernetes for quicker development
round trips.

.. seealso::
    Read more about how to configure Docker Compose in the
    official `Docker documentation <https://docs.docker.com/compose/>`_

Prerequisites
-------------
- Registered Auth0 account.
- Docker installed.

Step by Step
------------

* Create :ref:`application.yaml`
* Create Traefik configuration for ForwardAuth.
* Create a Docker-Compose configuration file.

.. seealso::
    I have created two examples of docker-compose configuration,
    one for Traefik 1.0 and one for Traefik 2.0.

    Check out the examples below

    - :ref:`traefik2-docker-compose-example`
    - :ref:`traefik1-docker-compose-example`
