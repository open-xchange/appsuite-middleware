---
title: Quota
icon: fas fa-chart-pie
tags: Limits, Configuration, Installation
---

This article attempts to outline the supported quota options that are available in order to specify/define limitations/restrictions for various resources.

In general, there are two types of possible quota restrictions:

 - ``Size``
 - ``Amount``

``Size`` specifies a limit based on the space that is available to store/hold items. Typically defined in number of bytes.

``Amount`` refers the maximum number of individual items that are allowed to be stored regardless of their actual size.

# Mail

The quota for mail module simply reflects the existing quota capabilities of the mail service that apply to a certain login/account. In case of IMAP the response for the GETQUOTAROOT command that signals the existing quota restrictions for STORAGE (``Size``) and MESSAGE (``Amount``) quota types.

It is not possible to configure the mail service's quota through Open-Xchange Middleware. Instead, the quota is supposed to be configured on the mail service itself. Open-Xchange Middleware only accesses that information.

# Objects/items (per context)

The quota for objects/items only covers the ``Amount`` quota type. It specifies how many objects of a certain module are allowed to be created for a single context. Hence, it is not possible to specify any quota restrictions for objects/items for an individual user. Since quota restrictions apply to a whole context, the purpose of such quotaq is to define a rough upper limit that is unlikely being reached during normal operation. Therefore it is rather supposed to prevent from excessive item creation (e.g. a synchronizing client running mad), but not intended to have a fine-grained quota setting. Thus exceeding that quota limitation will cause an appropriate exception being thrown, denying to further create any appointment in affected context.

Currently supported objects/items:

 - Appointments
 - Tasks
 - Contacts
 - Drive documents (only primary Drive account, not for subscribed ones)
 - Attachments of PIM (calendar, task, and contact) objects
 - Guest accounts
 - Share links

## Setting context objects/items quotas per configuration

The quota for objects/items is configured through the following properties:

 - [com.openexchange.quota.calendar](/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.quota.calendar)
 - [com.openexchange.quota.task](/config{{ site.baseurl }}/index.html#com.openexchange.quota.task)
 - [com.openexchange.quota.contact](/config{{ site.baseurl }}/index.html#com.openexchange.quota.contact)
 - [com.openexchange.quota.infostore](/config{{ site.baseurl }}/index.html#com.openexchange.quota.infostore)
 - [com.openexchange.quota.attachment](/config{{ site.baseurl }}/index.html#com.openexchange.quota.attachment)
 - [com.openexchange.quota.invite_guests](/config{{ site.baseurl }}/index.html#com.openexchange.quota.invite_guests)
 - [com.openexchange.quota.share_links](/config{{ site.baseurl }}/index.html#com.openexchange.quota.share_links)

Moreover, these properties are config-cascade aware and thus it is possible to set/apply certain values to server, context-set and context scopes (user scope is not supported as values apply to contexts and not to individual users). Thus Open-Xchange Middleware also ships with a ``quota.yml`` file located at ``/opt/open-xchange/etc/contextSets`` to specify object/items quotas for a set of contexts (see [Context Set Scope](http://oxpedia.org/wiki/index.php?title=ConfigCascade#Specifying_Configuration_-_Context_Set_Scope)).

In addition, setting context scope wise object/item quotas is performed through [``createcontext`` and ``changecontext`` command-line tools](http://oxpedia.org/wiki/index.php?title=ConfigCascade#Specifying_Configuration_-_Context_Scope_and_User_Scope):

For setting/changing a quota for an individual context
```
$ createcontext [...] --config/com.openexchange.quota.calendar=100000
$ changecontext [...] --config/com.openexchange.quota.calendar=100000
```

For removing a quota for an individual context
```
$ changecontext [...] --remove-config/com.openexchange.quota.calendar
```

## Setting context objects/items quotas per provisioning

Beside the possibility to configuration-wise set the quota resrtiction for certain objects/items, it is also possible to set limits through provisioning interfaces (command-line tools, SOAP).

Example to set a quota:
```
changecontext [...] --quota-module calendar --quota-value 100000
```

Example to unset/remove a quota:
```
changecontext [...] --quota-module calendar --quota-value -1
```

The values explicitly set through provisioning interfaces overrule the ones availbale from configuration.

Supported quota modules are:

 - calendar
 - task
 - contact
 - infostore
 - attachment
 - invite_guests
 - share_links

# Objects/items (per user)

In addition to the "Per context object/items" there are also ``Amount`` quotas that apply to user scope.

Currently supported objects/items:

 - Snippets

## Setting user objects/items quotas per configuration

The quota for objects/items is configured through the following properties:

 - [com.openexchange.snippet.quota.limit](/config{{ site.baseurl }}/index.html#com.openexchange.snippet.quota.limit)

Moreover, these properties are config-cascade aware and thus it is possible to set/apply certain values to server, context-set, context and user scopes.

Setting user scope wise object/item quotas is performed through [``createuser`` and ``changeuser`` command-line tools](http://oxpedia.org/wiki/index.php?title=ConfigCascade#Specifying_Configuration_-_Context_Scope_and_User_Scope):

For setting/changing a quota for an individual user
```
$ createuser [...] --config/com.openexchange.snippet.quota.limit=5
$ changeuser [...] --config/com.openexchange.snippet.quota.limit=5
```

For removing a quota for an individual user
```
$ changeuser [...] --remove-config/com.openexchange.snippet.quota.limit
```

## Setting user objects/items quotas per provisioning

Currently setting user objects/items quotas per provisioning is not supported.

# File storage

The quota for file storages covers the ``Size`` quota type. The file storage is used to store files for arbitrary modules/objects. Thereof:

 - Drive files
 - PIM attachments
 - Snippets
 - Resource cache (e.g. thumbnail images)
 - Mail compose attachments
 - ...

Except Drive files and Mail compose attachments all other files apply to the quota that is bound to a context.

Drive files are special here, since it is also possible to define a user-associated file storage that carries its own user-sensitive quota. Moreover to Drive files also applies an ``Amount`` quota through the objects/items quota outlined in previous sections.

Mail compose attachments are quota-less and thus do not count to the quota that is bound to a context. To see storage possibilites for mail compose attachments check [this]({{ site.baseurl }}/middleware/mail/mail_compose.html) arcticle.

The file storage quotas is exclusively set through provisioning via [``createcontext``/``changecontext``](http://oxpedia.org/wiki/index.php?title=AppSuite:Context_management#Parameters) and ``createuser``/``changecuser`` command-line tools.

# Snippet

Until middleware version 7.8.3 the size of snippets counted towards the quota of the filestore. For most cases this approach was suitable, but in some other scenarios it caused some troubles. For example when a user recieved some extra filestore space for a period of time (e.g. a pro trial) which timed out. In this scenario the user wasn't able to create any snippets (e.g. signatures) anymore if the usage was above his old limit. 

With middleware 7.8.4 this behaviour can now be changed. The middleware still stores the snippets within the filestore, but now it can use a dedicated quota for it instead. In order to active this quota you need to configure the following properties and restart the middleware.

```
com.openexchange.snippet.filestore.quota.mode=dedicated
com.openexchange.snippet.filestore.quota.perUserLimit=5MB
```

The first property activates the dedicated quota mode and the second one defines the quota limit for this quota. See [config docu](/components/middleware/config{{ site.baseurl }}/index.html#mode=search&term=com.openexchange.snippet.filestore.quota) for more informations about those properties.

Please note that the `com.openexchange.snippet.mime.groupware.SnippetSizeColumnUpdateTask` update task has to be run before the 'dedicated' quota mode can be activated.


When the quota is needed for the first time (any update/create snippete operation) the usage of snippets are calculated and stored within the `snippet` table. Please note that the usage of old snippets still applies to the filestore quota. If you changed the quota mode you need to run either the `checkconsistency` tool or better the new `recalculatefilestoreusage` command-line tool. Both tools are able to properly recalculate the filestore usage. The new tool let you recalculate single filestores, filestores in a context or all at once. You can even recalculate only context-filestores or only user-filestores.

```
usage: recalculatefilestoreusage [-c <context-id> [-u <user-id>] | --scope
                                 <scope>]
 -A,--adminuser <arg>         Admin username
 -c,--context <arg>           The identifier of the context for which the
                              file storage usage shall be recalculated. If
                              a user identifier is also specified, only
                              the user-associated file storage is
                              considered.
 -h,--help                    Prints a help text
 -P,--adminpass <arg>         Admin password
 -p,--port <arg>              The optional RMI port (default:1099)
    --responsetimeout <arg>   The optional response timeout in seconds
                              when reading data from server (default: 0s;
                              infinite)
 -s,--server <arg>            The optional RMI server (default: localhost)
    --scope <scope>           Scope can be either set to either 'all',
                              'context' or 'user'. If set to 'all', all
                              usages of all context and user file stores
                              are recalculated. Otherwise the usages of
                              either context- or user-associated file
                              storages are recalculated. Cannot be used in
                              conjunction with the '--context' or
                              '--user'.
 -u,--user <arg>              The identifier of the user for which the
                              file storage usage shall be recalculated or
                              'all' to recalculates the usages for all
                              user-associated file storage in the given
                              context.
The command-line tool to recalculate the usage of file storages
```

# Unified Quota

This section attempts to outline the unified quota option that is available with Middleware Core v7.8.4 and Cloud-Plugins extensions running on an OXaaS platform.

Currently, the unified quota feature can only be enabled for a user, if that user has an individual file storage configured. See [File Storages per user](https://oxpedia.org/wiki/index.php?title=AppSuite:File_Storages_per_User) to see how to enable/configure a dedicated file storage for a user.

If unified quota is enabled/available for a certain user, the value ``"unified"`` is advertised to clients through ``"io.ox/core//quotaMode"`` JSlob path (otherwise that path carries the value ``"default"``)

## Prerequisites

Setup & configure the Cassandra connector through installing package `open-xchange-cassandra` and setting the properties:

 - [com.openexchange.nosql.cassandra.clusterContactPoints](/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.nosql.cassandra.clusterContactPoints)
 - [com.openexchange.nosql.cassandra.port](/config{{ site.baseurl }}/index.html#com.openexchange.nosql.cassandra.port)

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