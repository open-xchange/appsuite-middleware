---
title: Unified quota
---

This article attempts to outline the unified quota option that is available with Middleware Core v7.8.4 and Cloud-Plugins extensions running on an OXaaS platform.

Currently, the unified quota is only applicable for contexts/tenants, in which every user has its own Drive quota configured. See [File Storages per user](https://oxpedia.org/wiki/index.php?title=AppSuite:File_Storages_per_User) to see how to enable/configure a dedicated file storage for a user.

If unified quota is enabled/available for a certain user, the value ``"unified"`` is advertised to clients through ``"io.ox/core//quotaMode"`` JSlob path (otherwise that path carries the value ``"default"``)

## Prerequisites

Setup & configure the Cassandra connector through installing package `open-xchange-cassandra` and setting the properties:

 - [com.openexchange.nosql.cassandra.clusterContactPoints](https://documentation.open-xchange.com/components/middleware/config/{{version}}/index.html#com.openexchange.nosql.cassandra.clusterContactPoints)
 - [com.openexchange.nosql.cassandra.port](https://documentation.open-xchange.com/components/middleware/config/{{version}}/index.html#com.openexchange.nosql.cassandra.port)

*(There a more Cassandra properties to set. See [Cassandra configuration](https://documentation.open-xchange.com/components/middleware/config/develop/index.html#mode=features&feature=Cassandra))*

## Installation

Install the v7.8.4 compliant `open-xchange-cloudplugins` package, which contains the `com.openexchange.cloudplugins.unifiedquota` bundle.

Enable to use Cassandra in Cloud-Plugins' configuration:

 - `com.openexchange.cloudplugins.useCassandra=true` in file `cloudplugins-cassandra.properties`

As stated before, unified quota is only applicable for contexts/tenants, in which every user has its own Drive quota configured. Moreover, those users are required to have `com.openexchange.unifiedquota.enabled` configuration option be set to `true` (which is [config-cascade aware](http://oxpedia.org/wiki/index.php?title=ConfigCascade)).