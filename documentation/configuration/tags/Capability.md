---
title: Capability
---

This page shows all properties with the tag: Capability

| __Key__ | permissions |
|:----------------|:--------|
| __Description__ | Default permissions for all users<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Permission.html">Permission</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Capability.html">Capability</a> |
| __File__ | permissions.properties |

---
| __Key__ | com.openexchange.capability.boring |
|:----------------|:--------|
| __Description__ | Mark this installation as boring, i.e. disable an easter egg<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Permission.html">Permission</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Capability.html">Capability</a> |
| __File__ | permissions.properties |

---
| __Key__ | com.openexchange.capability.alone |
|:----------------|:--------|
| __Description__ | The property "alone" signals that the user is the only user in associated context/tenant.<br>It disables certain collaborative features and that would therefore be useless in such a context/tenant.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Permission.html">Permission</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Capability.html">Capability</a> |
| __File__ | permissions.properties |

---
| __Key__ | com.openexchange.capability.share_links |
|:----------------|:--------|
| __Description__ | Allows users to create share links to share files or folders.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Permission.html">Permission</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Capability.html">Capability</a> |
| __File__ | permissions.properties |

---
| __Key__ | com.openexchange.capability.invite_guests |
|:----------------|:--------|
| __Description__ | Allows users to share files or folder with guest users.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Permission.html">Permission</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Capability.html">Capability</a> |
| __File__ | permissions.properties |

---
| __Key__ | com.openexchange.capability.archive_emails |
|:----------------|:--------|
| __Description__ | Enables/disables the archive functionalities that is to move on demand older mails from any selected folder to a special archive folder.<br>If disabled the server will reject to perform archive requests.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Permission.html">Permission</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Capability.html">Capability</a> |
| __File__ | permissions.properties |

---
| __Key__ | com.openexchange.capability.drive |
|:----------------|:--------|
| __Description__ | Enables or disables the "drive" module capability globally. The capability<br>can also be set more fine-grained via config cascade. Per default it is only<br>enabled for users that have the "infostore" permission set. This is configured<br>in /opt/open-xchange/etc/contextSets/drive.yml.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Capability.html">Capability</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.mail.categories |
|:----------------|:--------|
| __Description__ | General capability to enable/disable mail categories for primary inbox<br> |
| __Default__ | false |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Capability.html">Capability</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.messaging.enabled |
|:----------------|:--------|
| __Description__ | Determines whether messaging is enabled for this server.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Messaging.html">Messaging</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Capability.html">Capability</a> |
| __File__ | messaging.properties |

---
