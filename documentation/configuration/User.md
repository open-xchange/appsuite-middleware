---
title: User
---

This properties can be used to configure defaults used if some user has the according preference not defined.


| __Key__ | com.openexchange.user.beta |
|:----------------|:--------|
| __Description__ | Specifies whether beta features are enabled/disabled per default. The value is remembered for every user in its attributes.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a> |
| __File__ | user.properties |

---
| __Key__ | com.openexchange.folder.tree |
|:----------------|:--------|
| __Description__ | Defines the default folder tree that should be used if a user has not selected one.<br> |
| __Default__ | 1 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | user.properties |

---
| __Key__ | com.openexchange.user.contactCollectOnMailAccess |
|:----------------|:--------|
| __Description__ | Define the default behavior whether to collect contacts on mail access.<br>Note: Appropriate user access permission still needs to be granted in order to take effect.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | user.properties |

---
| __Key__ | com.openexchange.user.contactCollectOnMailTransport |
|:----------------|:--------|
| __Description__ | Define the default behavior whether to collect contacts on mail transport.<br>Note: Appropriate user access permission still needs to be granted in order to take effect.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Transport.html">Transport</a> |
| __File__ | user.properties |

---
| __Key__ | com.openexchange.user.maxClientCount |
|:----------------|:--------|
| __Description__ | Specify the max. allowed number of client identifiers stored/tracked per user.<br>A value equal to or less than zero means unlimited.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | user.properties |

---
