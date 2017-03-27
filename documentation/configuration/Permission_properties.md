---
title: Permission properties
---

The properties for permissions


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
| __Key__ | com.openexchange.capability.mobile_mail_app |
|:----------------|:--------|
| __Description__ | Mobile App general permission<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Permission.html">Permission</a> |
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
