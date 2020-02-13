File provider configuration
===========================

Enable the File provider in Traefik
-----------------------------------

.. seealso::
    `Traefik 2.x File provider documentation <https://docs.traefik.io/providers/file/>`_

.. literalinclude:: ../../../example/traefik2/traefik.yml
   :language: yaml
   :lines: 19-21

Configure the forwardauth middleware
------------------------------------
.. seealso::
    `Traefik 2.x official documentation <https://docs.traefik.io/middlewares/forwardauth/>`_

.. literalinclude:: ../../../example/traefik2/traefik-file.yml
   :language: yaml
   :emphasize-lines: 32-54

