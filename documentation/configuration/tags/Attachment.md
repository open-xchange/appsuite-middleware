---
title: Attachment
---

This page shows all properties with the tag: Attachment

| __Key__ | com.openexchange.quota.attachment |
|:----------------|:--------|
| __Description__ | Specifies the quota for the number of attachments bound to PIM objects that are allowed being created within a single context (tenant-wise scope).<br><br>The purpose of this quota is to define a rough upper limit that is unlikely being reached during normal operation.<br>Therefore it is rather supposed to prevent from excessive item creation (e.g. a synchronizing client running mad),<br>but not intended to have a fine-grained quota setting. Thus exceeding that quota limitation will cause an appropriate<br>exception being thrown, denying to further create any attachment in affected context.<br> |
| __Default__ | 250000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Attachment.html">Attachment</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | quota.properties |

---
| __Key__ | com.openexchange.snippet.rdb.supportsAttachments |
|:----------------|:--------|
| __Description__ | Specify whether the database-backed snippet implementation is supposed to support attachments.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Snippets.html">Snippets</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Attachment.html">Attachment</a> |
| __File__ | snippets.properties |

---
| __Key__ | MAX_UPLOAD_SIZE |
|:----------------|:--------|
| __Description__ | If the sum of all uploaded files (for contacts, appointments or tasks) in one request is larger than this value,<br>the upload will be rejected. If this value is not set or -1, the more general MAX_UPLOAD_SIZE configured in<br>server.properties will be used. If that value is 0 uploads will be unrestricted.<br>The size is in Bytes.<br> |
| __Default__ | 10485760 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Attachment.html">Attachment</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contacts.html">Contacts</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Tasks.html">Tasks</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointments.html">Appointments</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | attachment.properties |

---
