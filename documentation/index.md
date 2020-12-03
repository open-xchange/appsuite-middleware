---
title: Middleware Documentation
description:
---

Welcome to the documentation about the inner workings of the Java-based middleware platform of OX App Suite. This technical documentation covers articles about different topics and features, grouped by different subtopics on the left.

# New and Noteworthy in 7.10.5

The following gives an overview about some of the more noteworthy changes introduced in this release.

## Draft-based Mail Compose

With 7.10.2 a new backend implementation for composing emails in the webmailer was introduced, to provide contemporary features like instant attachment upload, attachment previews and listing of abandoned compose windows after login.

The original implementation was based on App Suite database and filestore and produced real draft emails only on explicit user requests. To address operational and usability concerns, an alternative mail compose backend implementation based on real draft emails is now provided. The new approach operates solely on draft emails in users "Drafts" folders and is enabled by default. It is subject to replace the former mechanism.

**The database and filestore-based mechanism is herewith declared as deprecated and subject for removal in a future release.**

The new mechanism can be disabled – and therefore the former one still being used instead – by setting `com.openexchange.mail.compose.mailstorage.enabled = false`.

**Please note** that the former approach is still used for all still open composition spaces. Do not decommission database and filestore resources until no more spaces are managed by the old mechanism.

## Federated Sharing

OX App Suite 7.10.5 introduces the new concept of "*Federated Sharing*" which allows a user to integrate a received share from another context
or another server into his own account and access these data in the same way as regular data.
This provides a seamless integration of data shared from a different context or server without using the guest interface.

The new *Federated Sharing* feature is part of of the ``open-xchange-subscribe`` package which needs to be installed in order to use it.

OX App Suite can handle two different share modes: The **cross-context** mode and the **cross-ox** mode.
The **cross-context** mode is chosen when a share comes from a different context on the same OX server.
The server will use internal services to access the shared data in the other context. No remote communication is required in this case.  
The **cross-ox** mode is used when a share comes from *another* OX server. The communication between the OX servers will then take place over the HTTP API.
**Please note** that the integration of calendar shares is currently only available in **cross-context** mode.

The following properties needs to be set in order to enable the **cross-context** feature:

* <code>[com.openexchange.capability.filestorage_xctx](https://documentation.open-xchange.com/components/middleware/config/7.10.5/#com.openexchange.capability.filestorage_xctx) = true</code>

* <code>[com.openexchange.calendar.xctx2.enabled](https://documentation.open-xchange.com/components/middleware/config/7.10.5/#com.openexchange.calendar.xctx2.enabled) = true</code>


The following properties needs to be set in order to enable the **cross-ox** feature:

* <code>[com.openexchange.capability.filestorage_xox](https://documentation.open-xchange.com/components/middleware/config/7.10.5/#com.openexchange.capability.filestorage_xox)=true</code>


See [Federated-Sharing](https://documentation.open-xchange.com/components/middleware/config/7.10.5/#mode=tags&tag=Federated%20Sharing) for a complete list of configuration options related to Federated Sharing.

See [Sharing and Guest Mode](https://documentation.open-xchange.com/7.10.5/middleware/miscellaneous/sharing_and_guest_mode.html) for more information about sharing and federated sharing features.


## Reseller Scope in Config-Cascade

OX Cloud offers a level "above" the `context` to support different resellers. Certain configuration might be related in OX App Suite to this level and this is not yet supported. Therefore, with 7.10.5, a new scope was introduced, i.e. the `reseller` scope to bridge the gap between the `context` and `contextSets` scopes.

For this to work, the `open-xchange-reseller` package must be installed. If so, the config cascade lookup order goes  from most to least specific: user (API) -> context (API) -> reseller (API) -> contextSet (Configuration) -> server (Configuration) -> default (Code).

If a reseller does not have a value for a given config, then its parents (if exist) are iterated until a value is available. If no value can be found then the next scope down the line will be used, i.e. `contextSets`.

To set properties, taxonomies and capabilities in the `reseller` scope, the `createadmin` and `changeadmin` command line tools can be used:

```bash
$ createadmin [...] --config/com.openexchange.oauth.twitter=false --config/com.openexchange.oauth.google=attributes --capabilities-to-add "portal, -autologin" --taxonomy/types=some-taxonomy
$ changeadmin [...]  --config/com.openexchange.oauth.google=attributes --remove-config/com.openexchange.oauth.twitter --capabilities-to-add "portal" --capabilities-to-drop "autologin" --taxonomy/types=some-taxonomy --remove-taxonomy/types=hosting
```

Note that the difference between `remove` and `drop` in capabilities is that with remove the capability will be removed from a set if it was set in a less narrower scope, see "-" prefix; while with drop the entry will be erased from the database table and its default behavior will be restored.

No operations are allowed on properties starting with `com.openexchange.capability.` and no capabilities are allowed that match a user permission bit.

More information about ConfigCascade can be found [here](https://documentation.open-xchange.com/7.10.5/middleware/miscellaneous/config_cascade.html).

## Configuration Changes for Logback

As of version 7.10.5 of the AppSuite (and version 2.0.0 of the Open-Xchange Java Commons libraries) there is a slight change in the property naming of the `logback.xml` file. The changes are as follows:

* The base package of all the extensions is further refined into sub-packages each containing a specific extension. There are four in total:
  * `appenders`: Contains all available supported appenders
     * Logstash (since 7.6.2)
     * Kafka (since 7.10.5)
  * `converters`: Contains all available supported converters
      * `ExtendedReplacingCompositeConverter`
      * `LineMDCConverter`
      * `LogSanitisingConverter`
      * `ThreadIdConverter`
  * `encoders`: Contains all available supported encoders
      * `ExtendedPatternLayoutEncoder`
      * `JSONEncoder`
  * `policies`: Contains all available supported policies
      * `FixedWindowRollingPolicy`
* The property and class names of the four previously mentioned extensions has changed to reflect the package name. In particular, the fully qualified property and class name now contains the package name, AND in case of an appender, the appender name as well. For example, the `FixedWindowRollingPolicy`, previously configured as `<rollingPolicy class="com.openexchange.logback.extensions.FixedWindowRollingPolicy">` , will now be configured as `<rollingPolicy class="com.openexchange.logback.extensions.policies.FixedWindowRollingPolicy">`. For an extended list on how the names were changed and how to update your configuration files, please refer to the [Upgrade Guide](https://documentation.open-xchange.com/7.10.5/middleware/monitoring/05_logging.html#upgrading-from-an-older-version).

Since 7.10.5, there is also a new appender available, namely "Kafka" which can directly write log entries to a Kafka topic in a Kafka cluster. The appender's class name is `com.openexchange.logback.extensions.appenders.KafkaAppender` and can be configured as follows:

```xml
<appender name="KAFKA" class="com.openexchange.logback.extensions.appenders.kafka.KafkaAppender">
    <servers>kafka-node01:9092,kafka-node02:9092</servers>
    <topic>logs</topic>
    <key>node03</key>
</appender>
```
For more information about logging with Logstash and Kafka can be found [here](https://documentation.open-xchange.com/7.10.5/middleware/monitoring/05_logging.html).

## Improved API access with OAuth 2.0

Since version v7.8.0 the OX App Suite is able to act as an OAuth 2.0 provider to allow access to certain API calls.
With version v7.10.5 support for external authorization servers has been improved, the endpoints that can be accessed with OAuth have been expanded and their API documentation has been further enriched.

Which endpoints are available with OAuth and which permission scopes are required for the respective api calls can be found in the current [API documentation](https://documentation.open-xchange.com/components/middleware/http/latest/index.html).

Regarding the support of external authorization servers it is now possible to process and validate both JSON Web Token (JWT) and opaque access tokens issued by an external Identity & Access management system (e.g. [Keycloak](https://www.keycloak.org/)).

As before, the OAuth 2.0 provider is activated by the property:

* <code>com.openexchange.oauth.provider.enabled</code>

The configuration property <code>com.openexchange.oauth.provider.isAuthorizationServer</code>
is no longer available and was transferred to <code>com.openexchange.oauth.provider.mode</code>, which now configures the OAuth provider.

The following is a list of possible configurations:

* <code>com.openexchange.oauth.provider.mode</code>
 Besides the functionality to act as an OAuth 2.0 provider, there are three different modes to choose from:
  * <code>auth_server</code>
 Defines whether the enabled OAuth 2.0 provider does not only act as resource server but also as authorization server.
  * <code>expect_jwt</code>
 Defines whether the enabled OAuth 2.0 provider is accesible with OAuth 2.0 Bearer Access Tokens that are JWTs, which were issued by an external Identity & Access management System.
   * <code>token_introspection</code>
 Defines whether the enabled OAuth 2.0 provider is able to grant access based on opaque bearer tokens through token introspection.

See [OAuth 2.0 Operator Guide](https://documentation.open-xchange.com/7.10.4/middleware/login_and_sessions/oauth_2.0_provider/01_operator_guide.html#using-an-external-authorization-server) for a complete list of configuration options.

## Upgrade to Hazelcast 4

Hazelcast is upgraded to v4.1, which comes with a new network protocol that causes compatibility problems with nodes running the previous version of the Hazelcast library in the cluster, leading to the Hazelcast subsystem not starting up on the *newer* node. Therefore, it is required to separate the clusters from each other during *rolling* upgrade scenarios where middleware nodes are updated to the new version and restarted one after the other.

Internally, this separation is enforced by applying a special offset to the port Hazelcast will use for connecting to other nodes in the cluster. In case there are no restrictions towards the ports that can be used by middleware processes to communicate with each other, a default automatic port offset mechanism can be used and no manual changes are necessary. However, it is still possible to take full control over the used ports if needed, e.g. because firewalls have to be considered.

For this purpose, the new lean configuration property [``com.openexchange.hazelcast.network.portOffset``](https://documentation.open-xchange.com/components/middleware/config/7.10.5/#com.openexchange.hazelcast.network.portOffset) is introduced, which allows to configure an explicit offset that is applied to the configured listening port. With the default setting ``auto``, the offset is chosen automatically by the application, based on the shipped Hazelcast version (``4`` with the upcoming update to Hazelcast 4.1). It can still also be set to ``0`` to use the port from [``com.openexchange.hazelcast.network.port``](https://documentation.open-xchange.com/components/middleware/config/7.10.5/#com.openexchange.hazelcast.network.port) as-is, if one needs more control due to firewalls etc. However, one should then ensure that nodes running the previous version do use a different port, or all 'old' nodes in the cluster are shut down prior starting the first 'new' one. A similar option is also introduced for the supplementary package ``open-xchange-cluster-upgrade-from-7103-7104``.

And, since the upgraded middleware nodes will effectively form a separate cluster after a restart, this also means that volatile data like user sessions being held in the distributed session storage won't be migrated, requiring end users to re-login after the update (unless managed by an external SSO-system). Also, please obey the general recommendations when performing a [cluster upgrade with breaking Hazelcast upgrade](https://documentation.open-xchange.com/7.10.5/middleware/administration/running_a_cluster.html#rolling-upgrade-with-breaking-hazelcast-upgrade).

## FULLTEXT index in InfoStore

With Open-Xchange server v7.10.5 a ``FULLTEXT`` index to improve searching for files in the Drive module was introduced. This index significantly improves the duration of a search request, especially if many files are stored and is created on the ``infostore_document`` table as soon as this feature is enabled via configuration. For more details and a configuration guide see [MySQL Fulltext Index](https://documentation.open-xchange.com/7.10.5/middleware/administration/mysql_fulltext_index.html).

## Changed default values for Drive folder move permission modes
With Open-Xchange server v7.10.5 the default values for Drive folder move permission modes were changed. The new default value for the properties ``com.openexchange.folderstorage.permissions.moveToPublic``, ``com.openexchange.folderstorage.permissions.moveToPrivate`` and ``com.openexchange.folderstorage.permissions.moveToShared`` is now ``inherit``, so all folders will inherit the permissions from the new parent folder on move. For more details see [Drive folder permission mode on folder move] (https://documentation.open-xchange.com/7.10.5/middleware/miscellaneous/drive_folder_permission_modes_on_move.html).
