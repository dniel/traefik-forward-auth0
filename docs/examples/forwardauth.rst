ForwardAuth
===========

In the simplest form of supplying a configuration file for ForwardAuth its
enough to put an application.yaml file like described in `Spring Boot Application Property Files examples`_

Quoted from the Spring Boot documentation
    SpringApplication loads properties from application.properties files in the following locations and adds them to the Spring Environment:

    - A /config subdirectory of the current directory
    - The current directory
    - A classpath /config package
    - The classpath root



.. seealso:: Spring Boot official documentation
    ForwardAuth is using the Spring Boot application framework and uses its externalized
    configuration features. See the `Spring Boot documentation of externalized configuration`_
    for a complete description of all externalized configuration features.

Complete example of ForwardAuth configuration file
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. literalinclude:: ../../example/application.yaml
   :language: yaml

.. _Spring Boot documentation of externalized configuration: https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config

.. _Spring Boot Application Property Files examples: https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config-application-property-files