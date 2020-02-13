Install using Helm chart
========================

.. note::




Add the Helm repo
-----------------

.. code-block:: shell-session

   $ helm repo add dniel https://dniel.github.io/charts/

Install chart with
------------------

.. code-block:: shell-session

   $ helm install --name my-release forwardauth dniel/forwardauth -f values.yaml
