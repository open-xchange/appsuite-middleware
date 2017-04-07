---
title: Group
---

This page shows all properties with the tag: Group

| __Key__ | GID_NUMBER_START |
|:----------------|:--------|
| __Description__ | Set to higher than 0 to enable gid number feature.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Group.html">Group</a> |
| __File__ | Group.properties |

---
| __Key__ | CHECK_GROUP_UID_FOR_NOT_ALLOWED_CHARS |
|:----------------|:--------|
| __Description__ | If set to true this will check the group name using the check defined in CHECK_GROUP_UID_REGEXP.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | CHECK_GROUP_UID_REGEXP |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Group.html">Group</a> |
| __File__ | Group.properties |

---
| __Key__ | CHECK_GROUP_UID_REGEXP |
|:----------------|:--------|
| __Description__ | If CHECK_GROUP_UID_FOR_NOT_ALLOWED_CHARS is set to true group names will be checked against this regex.<br> |
| __Default__ | [ $@%\.+a-zA-Z0-9_-] |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | CHECK_GROUP_UID_FOR_NOT_ALLOWED_CHARS |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Group.html">Group</a> |
| __File__ | Group.properties |

---
| __Key__ | AUTO_TO_LOWERCASE_UID |
|:----------------|:--------|
| __Description__ | This will lowercase the uid.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | CHECK_GROUP_UID_FOR_NOT_ALLOWED_CHARS |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Group.html">Group</a> |
| __File__ | Group.properties |

---
