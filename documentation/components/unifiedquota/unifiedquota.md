---
title: Unified quota
---

This article attempts to outline the unified quota option that is available with Middleware Core v7.8.4 and Cloud-Plugins extensions running on an OXaaS platform.

Currently, the unified quota is only applicable for contexts/tenants, in which every user has its own Drive quota configured. See [File Storages per user](https://oxpedia.org/wiki/index.php?title=AppSuite:File_Storages_per_User) to see how to enable/configure a dedicated file storage for a user.

If unified quota is enabled/available for a certain user, the value ``"unified"`` is advertised to clients through ``"io.ox/core//quotaMode"`` JSlob path (otherwise that path carries the value ``"default"``)

## Prerequisites

Setup & configure the Cassandra connector through installing package `open-xchange-cassandra` and setting the properties:

 - [com.openexchange.nosql.cassandra.clusterContactPoints](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.nosql.cassandra.clusterContactPoints)
 - [com.openexchange.nosql.cassandra.port](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.nosql.cassandra.port)

*(There a more Cassandra properties to set. See [Cassandra configuration](https://documentation.open-xchange.com/components/middleware/config/develop/index.html#mode=features&feature=Cassandra))*

## Installation

1. Install the v7.8.4 compliant `open-xchange-cloudplugins` package, which contains the `com.openexchange.cloudplugins.unifiedquota` bundle.

2. Setup Cassandra and create the required data structures as documented in the release documentation of the corresponding release [https://software.open-xchange.com/products/appsuite/doc/Cloud_Plugins_Release_Notes_for_Release_X.Y.Z_YYYY-MM-DD.pdf](https://software.open-xchange.com/products/appsuite/doc/)
3. Enable to use Cassandra in Cloud-Plugins' configuration. Option `com.openexchange.cloudplugins.useCassandra` is required to be set to `true` in file `cloudplugins-cassandra.properties`
4. Use one of the create methods of the OXResellerUserService SOAP API and
   1. Enable [dediacted file storage for the user](https://oxpedia.org/wiki/index.php?title=AppSuite:File_Storages_per_User#Creating_a_user_file_storage); e.g. through setting option `ALLOW_CHANGING_QUOTA_IF_NO_FILESTORE_SET` to `true` and applying the ``maxQuota`` value in order to activate the dedicated file storage per user. The value is in MB.
  2. Enable the ``com.openexchange.unifiedquota.enabled=true`` property for the user [through config-cascade](http://oxpedia.org/wiki/index.php?title=ConfigCascade); e.g. through setting ``config/com.openexchange.unifiedquota.enabled=true`` as user attribute
5. Use the ``setMailQuota`` method of the OXaaSService SOAP API and use the same value as in ``maxQuota`` in the previous method. This value is also in MB. This method finally activates the unified quota mode.

Users with Unified quota enabled cannot be deleted as long as ``config/com.openexchange.unifiedquota.enabled=true``. Use change method of the OXResellerUserService SOAP API and set this userAttribute to ``false``. Thereafter, it is possible to use the delete method to remove that user.