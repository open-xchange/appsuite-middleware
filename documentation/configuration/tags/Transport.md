---
title: Transport
---

This page shows all properties with the tag: Transport

| __Key__ | com.openexchange.mail.transportServerSource |
|:----------------|:--------|
| __Description__ | Set the transport server source for primary mail account; meaning which source is taken to determine the transport<br>server into which the user wants to login to transport mails. Set to 'global' to take<br>the value specified through property "com.openexchange.mail.transportServer". Set to<br>'user' to take user's individual transport server settings as specified in storage.<br>Currently known values: user and global<br> |
| __Default__ | user |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Transport.html">Transport</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.transportServer |
|:----------------|:--------|
| __Description__ | Primary transport server: e.g. 192.168.178.32:125 or smtp://192.168.178.32:225<br>Only takes effect when property "com.openexchange.mail.transportServerSource" is set to "global"<br> |
| __Default__ | 127.0.0.1 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Transport.html">Transport</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.transportStartTls |
|:----------------|:--------|
| __Description__ | Set if STARTTLS should be used when connecting to the primary transport server<br>Only takes effect when property "com.openexchange.mail.transportServerSource" is set to "global" <br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Transport.html">Transport</a> |
| __File__ | mail.properties |

---
