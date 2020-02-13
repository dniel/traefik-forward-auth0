Configuration
=============

.. _application.yaml:

Application configuration
-------------------------

The ForwardAuth application needs a configuration file called **application.yaml**
to run. The easiest way is to supply the configuration file like described by
`Spring Boot Application Property Files examples`_.

Quoted from the Spring Boot documentation
    SpringApplication loads properties from application.properties files in the following locations and adds them to the Spring Environment:

    - A /config subdirectory of the current directory
    - The current directory
    - A classpath /config package
    - The classpath root

.. seealso::
    ForwardAuth is built using the Spring Boot application framework. It has
    a built-in and very flexible externalized configuration feature,
    read the `Spring Boot documentation of externalized configuration`_ for a
    complete description of all Spring Boot configuration features.

Complete configuration file format
----------------------------------

.. literalinclude:: ../../example/application.yaml
   :language: yaml

.. _Spring Boot documentation of externalized configuration: https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config

.. _Spring Boot Application Property Files examples: https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config-application-property-files