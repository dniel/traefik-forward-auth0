File provider configuration
===========================

Enable the File provider in Traefik
-----------------------------------

.. seealso::
    Documentation.

    Traefik 2.x File provider documentation.
    https://docs.traefik.io/providers/file/

.. literalinclude:: ../../../example/traefik2/traefik.yml
   :language: yaml
   :lines: 19-21

Configure the forwardauth middleware
------------------------------------
.. seealso:: Traefik 2.x official documentation
    https://docs.traefik.io/middlewares/forwardauth/

.. literalinclude:: ../../../example/traefik2/traefik-file.yml
   :language: yaml
   :emphasize-lines: 32-54

