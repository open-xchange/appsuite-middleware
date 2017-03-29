---
title: Infostore
---

This page shows all properties with the tag: Infostore

| __Key__ | com.openexchange.quota.infostore |
|:----------------|:--------|
| __Description__ | Specifies the quota for the number of documents that are allowed being created within a single context (tenant-wise scope).<br><br>The purpose of this quota is to define a rough upper limit that is unlikely being reached during normal operation.<br>Therefore it is rather supposed to prevent from excessive item creation (e.g. a synchronizing client running mad),<br>but not intended to have a fine-grained quota setting. Thus exceeding that quota limitation will cause an appropriate<br>exception being thrown, denying to further create any document in affected context.<br> |
| __Default__ | 250000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Infostore.html">Infostore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | quota.properties |

---
| __Key__ | com.openexchange.mail.transport.publishingPublicInfostoreFolder |
|:----------------|:--------|
| __Description__ | Specify the name of the publishing infostore folder which is created below public infostore folder.<br>The denoted folder is created if absent only if "com.openexchange.mail.transport.enablePublishOnExceededQuota" is enabled.<br>The special identifier "i18n-defined" indicates to use translation of text "E-Mail attachments".<br> |
| __Default__ | i18n-defined |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.mail.transport.enablePublishOnExceededQuota |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Transport.html">Transport</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Infostore.html">Infostore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | transport.properties |

---
| __Key__ | com.openexchange.subscribe.microformats.infostore.http |
|:----------------|:--------|
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Microformats.html">Microformats</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Infostore.html">Infostore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Subscribe.html">Subscribe</a> |
| __File__ | microformatSubscription.properties |

---
| __Key__ | com.openexchange.subscribe.microformats.infostore.http.autorunInterval |
|:----------------|:--------|
| __Description__ | Defines the subscription refresh autorun interval for the microformats infostore.<br> |
| __Default__ | 1d |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Microformats.html">Microformats</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Infostore.html">Infostore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Subscribe.html">Subscribe</a> |
| __File__ | microformatSubscription.properties |

---
| __Key__ | MAX_UPLOAD_SIZE |
|:----------------|:--------|
| __Description__ | If the sum of all uploaded files in one request is larger than this value, the upload will be rejected.<br>If this value is not set or -1, the more general MAX_UPLOAD_SIZE configured in server.properties will be used.<br>If that value is 0 uploads will be unrestricted. The size is in Bytes.<br> |
| __Default__ | 10485760 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | MAX_UPLOAD_SIZE in server.properties |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Infostore.html">Infostore</a> |
| __File__ | infostore.properties |

---
| __Key__ | com.openexchange.infostore.zipDocumentsCompressionLevel |
|:----------------|:--------|
| __Description__ | Configures the used compression level that is applied to .zip containers<br>when downloading multiple documents at once. Possible values are "-1" for<br>the built-in default level, "0" for no compression, or any number between<br>"1" (best speed) and "9" (best compression).<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Infostore.html">Infostore</a> |
| __File__ | infostore.properties |

---
| __Key__ | com.openexchange.infostore.trash.retentionDays |
|:----------------|:--------|
| __Description__ | Specifies how many days items are kept in the trash folder until they are<br>finally deleted. Cleanup is triggered during user login. A value equal to or<br>smaller "0" disables automatic cleanup for items in the trash folder.<br>The default value configured here takes effect on server granularity, but<br>can be overwritten for sets of contexts, single contexts and single users.<br>See http://oxpedia.org/wiki/index.php?title=ConfigCascade for more<br>information.<br>Depending on the "protected" flag of the corresponding preference path,<br>users are able to change their configured value on their own. This can be<br>adjusted via /meta/infostore.yml<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Infostore.html">Infostore</a> |
| __File__ | infostore.properties |

---
