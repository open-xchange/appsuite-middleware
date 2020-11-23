---
title: Middleware Documentation
description: 
---

Welcome to the documentation about the inner workings of the Java-based middleware platform of OX App Suite. This technical documentation covers articles about different topics and features, grouped by different subtopics on the left.

# New and Noteworthy in 7.10.5

The following gives an overview about some of the more noteworthy changes introduced in this release.

## Draft-based Mail Compose

## Federated Sharing

## Reseller Scope in Config-Cascade

## Configuration Changes for Logback

## Improved API access with OAuth 2.0

## Upgrade to Hazelcast 4

Hazelcast is upgraded to v4.1, which comes with a new network protocol that causes compatibility problems with nodes running the previous version of the Hazelcast library in the cluster, leading to the Hazelcast subsystem not starting up on the *newer* node. Therefore, it is required to separate the clusters from each other during *rolling* upgrade scenarios where middleware nodes are updated to the new version and restarted one after the other.

Internally, this separation is enforced by applying a special offset to the port Hazelcast will use for connecting to other nodes in the cluster. In case there are no restrictions towards the ports that can be used by middleware processes to communicate with each other, a default automatic port offset mechanism can be used and no manual changes are necessary. However, it is still possible to take full control over the used ports if needed, e.g. because firewalls have to be considered.

For this purpose, the new lean configuration property [``com.openexchange.hazelcast.network.portOffset``](https://documentation.open-xchange.com/components/middleware/config/7.10.5/#com.openexchange.hazelcast.network.portOffset) is introduced, which allows to configure an explicit offset that is applied to the configured listening port. With the default setting ``auto``, the offset is chosen automatically by the application, based on the shipped Hazelcast version (``4`` with the upcoming update to Hazelcast 4.1). It can still also be set to ``0`` to use the port from [``com.openexchange.hazelcast.network.port``](https://documentation.open-xchange.com/components/middleware/config/7.10.5/#com.openexchange.hazelcast.network.port) as-is, if one needs more control due to firewalls etc. However, one should then ensure that nodes running the previous version do use a different port, or all 'old' nodes in the cluster are shut down prior starting the first 'new' one. A similar option is also introduced for the supplementary package ``open-xchange-cluster-upgrade-from-7103-7104``.

And, since the upgraded middleware nodes will effectively form a separate cluster after a restart, this also means that volatile data like user sessions being held in the distributed session storage won't be migrated, requiring end users to re-login after the update (unless managed by an external SSO-system). Also, please obey the general recommendations when performing a [cluster upgrade with breaking Hazelcast upgrade](https://documentation.open-xchange.com/7.10.5/middleware/administration/running_a_cluster.html#rolling-upgrade-with-breaking-hazelcast-upgrade).

## FULLTEXT index in InfoStore
