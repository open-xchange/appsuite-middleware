---
title: Mail Drive
---

| __Key__ | com.openexchange.file.storage.mail.enabled |
|:----------------|:--------|
| __Description__ | The general switch to enable/disable Mail Drive.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Drive.html">Mail Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestorage.html">Filestorage</a> |
| __File__ | filestorage-maildrive.properties |

---
| __Key__ | com.openexchange.file.storage.mail.fullNameAll |
|:----------------|:--------|
| __Description__ | Specifies the full name for the virtual folder, which provides the attachments from all messages.<br>Required.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Drive.html">Mail Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestorage.html">Filestorage</a> |
| __File__ | filestorage-maildrive.properties |

---
| __Key__ | com.openexchange.file.storage.mail.fullNameReceived |
|:----------------|:--------|
| __Description__ | Specifies the full name for the virtual folder, which provides the attachments from received messages.<br>Required.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Drive.html">Mail Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestorage.html">Filestorage</a> |
| __File__ | filestorage-maildrive.properties |

---
| __Key__ | com.openexchange.file.storage.mail.fullNameSent |
|:----------------|:--------|
| __Description__ | Specifies the full name for the virtual folder, which provides the attachments from sent messages.<br>Required.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Drive.html">Mail Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestorage.html">Filestorage</a> |
| __File__ | filestorage-maildrive.properties |

---
| __Key__ | com.openexchange.file.storage.mail.maxAccessesPerUser |
|:----------------|:--------|
| __Description__ | Specifies how many concurrent connections/accesses to IMAP store are allowed to be established for a single user.<br>A request exceeding that limitation will be paused until an acquired connection is released.<br> |
| __Default__ | 4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Drive.html">Mail Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestorage.html">Filestorage</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | filestorage-maildrive.properties |

---
