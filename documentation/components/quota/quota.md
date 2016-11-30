---
title: Available quota options
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

 - com.openexchange.quota.calendar (``quota.properties``)
 - com.openexchange.quota.task (``quota.properties``)
 - com.openexchange.quota.contact (``quota.properties``)
 - com.openexchange.quota.infostore (``quota.properties``)
 - com.openexchange.quota.attachment (``quota.properties``)
 - com.openexchange.quota.invite_guests (``share.properties``)
 - com.openexchange.quota.share_links (``share.properties``)

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

 - com.openexchange.snippet.quota.limit (``snippets.properties``)

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
 - ...

Except Drive files all other files apply to the quota that is bound to a context.

Drive files are special here, since it is also possible to define a user-associated file storage that carries its own user-sensitive quota. Moreover to Drive files also applies an ``Amount`` quota through the objects/items quota outlined in previous sections.

The file storage quotas is exclusively set through provisioning via [``createcontext``/``changecontext``](http://oxpedia.org/wiki/index.php?title=AppSuite:Context_management#Parameters) and ``createuser``/``changecuser`` command-line tools.