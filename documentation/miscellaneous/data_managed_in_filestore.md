---
title: Data managed in file storage
icon: fa-container-storage
tags: Configuration , Storage, Drive, Files
---

# Data managed in file storage

## Introduction

This paper attempts to outline what type of data is stored in file storage like NFS, S3, Scality or whatever storage connector is utilized.

## 2.Types of data held in file storage

### 2.1.PIM attachments

All file attachments bound to PIM objects (appointments, tasks, &amp; contacts) are stored to the context-associated file storage and therefore are accounted to the overall context-assigned quota usage.

The number of allowed attachments is configured through property `com.openexchange.quota.attachment`, which controls how many attachments are allowed tob e uploaded in context.

### Drive files

Files uploaded/managed in Drive module are either stored to the context-associated file storage, but may also be managed in a dedicated user-associated one to have a user-sensitive drive-only quota usage and limit.

### Resource cache

Application-generated files that are worth being cached (e.g. generated thumbnail images) are held in FIFO manner in a resource cache. Provided that the cache is enabled (`com.openexchange.preview.cache.enabled=true`) and configured to utilize the file storage through setting `com.openexchange.preview.cache.type` property to `FS` (default), then files associated with that resource cache are stored to the context-associated file storage and therefore are accounted to the overall context-assigned quota usage if setting `com.openexchange.preview.cache.quotaAware` is set to `true` (default is `false`).

Further configuration options are:

- `com.openexchange.preview.cache.quota`
Specifies the total quota for preview cache for each context. This option if config-cascade aware and thus allows specifying individual values. A value of zero or less means no quota. Default is `10485760` (10MB)

- `com.openexchange.preview.cache.quotaPerDocument`
Specifies the quota per document for preview cache for each context. This option if config-cascade aware and thus allows specifying individual values. A value of zero or less means no quota. Default is `524288` (512KB)


Moreover, an administrator is able to manually clear entries belonging to that cache either globally or for a certain context through the &#39;clearpreviewcache&#39; command-line tool.

### Snippets (aka Signatures)

In case the system signals that file storage is available through advertising `true` for property `com.openexchange.capability.filestore` created snippets/signatures are created in context-associated file storage and therefore are accounted to the overall context-assigned quota.

Available options:

- `com.openexchange.snippet.quota.limit`
Specifies the maximum number of snippets that are allowed being created by a single user. A value of less than 0 (zero) means unlimited. Default is `-1`.

- `com.openexchange.mail.signature.maxImageSize`
Specifies the maximum size (in MB) for one image contained in the HTML mail signature. Default is `1`

- `com.openexchange.mail.signature.maxImageLimit`
Specifies the maximum amount of images that are allowed in a single HTML mail signature. Default is `3`

### vCards

In order to support a lossless contact synchronization, the Open-Xchange Server supports to enable a vCard storage. A vCard associated with a contact managed in Open-Xchange contains all properties/attributes received from arbitrary clients that synchronize/exchange contacts with Open-Xchange through the vCard format.

If enabled through property `com.openexchange.contact.storeVCards` (default is `true`) the vCards are created in context-associated file storage and therefore are accounted to the overall context-assigned quota.

In addition the setting `com.openexchange.contact.maxVCardSize` defines the maximum allowed size of a (single) vCard file in bytes. vCards larger than the configured maximum size are rejected and not parsed by the server. A value of `0` or smaller is considered as unlimited. Default is `4194304` (4 MB).

### Mail compose attachment

Starting with v7.10.2 the Open-Xchange Server supports a new API for composing a new mail. Whenever the user starts a new mail a dedicated composition space is created, that reflects the current state of the mail.

Most remarkably such a composition space also supports instant attachment uploads. In contrast to former mail compose API, attachments are now stored and held in used storage as long as associated composition space is alive.

Available options:

- `com.openexchange.mail.compose.maxSpacesPerUser`
Specifies the max. number of concurrently opened composition spaces that are allowed for a single user. Default is 20. This configuration option is config-cascade aware and reloadable.

- `com.openexchange.mail.compose.maxIdleTimeMillis`
Defines the time span in milliseconds a composition space may be idle before it gets deleted. If zero or less than zero is specified, composition spaces may be idle forever. Can contain units of measurement: D(=days) W(=weeks) H(=hours) M(=minutes). Default is 1W (1 week). This configuration option is config-cascade aware and reloadable.

Those two configuration options specify how many composition spaces are allowed to be created and how long they are allowed to exist (when idling). To directly address storage possibilities, there two possible ways to operate:

#### Default (context-associated) storage

By default, uploaded attachments are stored in context-associated file storage, but without affecting its quota. Meaning, attachments are managed in context-associated file storage regardless of any quota limitations that were specified during context provisioning. This is the appropriate choice for installations, in which the registered file storage has plenty of space and has not been space-wise restricted according to quota usages.

If the context-associated file storage is used, the &quot;checkconsistency&quot; command-line tool cares about orphaned/non-existent references.

#### Dedicated storage

To have a dedicated file storage that is supposed to be used for uploaded attachments to not use the context-associated one, there is the opportunity to specify following configuration option:

- `com.openexchange.mail.compose.fileStorageId`
Specifies the identifier for a dedicated file storage that is supposed to be used for mail compose attachments. By default that option empty, but config-cascade aware and reloadable.

Once a dedicated file storage is used, the &quot;checkconsistency&quot; command-line tool is not effective for caring about orphaned/non-existent references. Instead, there is a dedicated tooling available named &quot;deleteorphanedattachments&quot;.