---
title: MAL Poll
---

| __Key__ | com.openexchange.push.malpoll.period |
|:----------------|:--------|
| __Description__ | Define the amount of time in milliseconds when to periodically check for new mails.<br> |
| __Default__ | 300000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/MAL_Poll.html">MAL Poll</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | malpoll.properties |

---
| __Key__ | com.openexchange.push.malpoll.global |
|:----------------|:--------|
| __Description__ | Whether a global timer is set or a timer per user.<br>Or in other words: Do you want a global heartbeat or a heartbeat per user?<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/MAL_Poll.html">MAL Poll</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | malpoll.properties |

---
| __Key__ | com.openexchange.push.malpoll.concurrentglobal |
|:----------------|:--------|
| __Description__ | Whether the tasks executed by global timer are executed concurrently<br>or by calling timer's thread.<br>Note: This property only has effect if "com.openexchange.push.malpoll.global"<br>is set to "true"<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.push.malpoll.global |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/MAL_Poll.html">MAL Poll</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | malpoll.properties |

---
| __Key__ | com.openexchange.push.malpoll.folder |
|:----------------|:--------|
| __Description__ | Define the folder to look-up for new mails in each mailbox.<br> |
| __Default__ | INBOX |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/MAL_Poll.html">MAL Poll</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | malpoll.properties |

---
