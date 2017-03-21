---
title: Filestore
---

This page shows all properties with the tag: Filestore

| __Key__ | AVERAGE_USER_SIZE |
|:----------------|:--------|
| __Description__ | The average file storage occupation for a user in MB.<br> |
| __Default__ | 100 |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Admin.html">Admin</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | AdminUser.properties |

---
| __Key__ | ALLOW_CHANGING_QUOTA_IF_NO_FILESTORE_SET |
|:----------------|:--------|
| __Description__ | Defines whether it is allowed to change the quota value for a user that has no individual file storage set<br><br>If set to "true" and the user has not yet an individual file storage set, an appropriate file storage gets<br>assigned to the user. This implicitly includes to move the user's files from context file storage to that<br>newly assigned file storage, which might be a long operation.<br> |
| __Default__ | false |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Admin.html">Admin</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | AdminUser.properties |

---
