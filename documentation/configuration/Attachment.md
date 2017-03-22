---
title: Attachment
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
