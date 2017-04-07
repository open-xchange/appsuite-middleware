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
| __Key__ | com.openexchange.mail.transport.referencedPartLimit |
|:----------------|:--------|
| __Description__ | Define the limit in bytes for keeping an internal copy of a referenced<br>MIME message's part when sending a mail. If a part exceeds this limit<br>a temporary file is created holding part's copy.<br> |
| __Default__ | 1048576 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Transport.html">Transport</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | transport.properties |

---
| __Key__ | com.openexchange.mail.defaultTransportProvider |
|:----------------|:--------|
| __Description__ | The transport provider fallback if an URL does not contain/define a protocol.<br> |
| __Default__ | smtp |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Transport.html">Transport</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | transport.properties |

---
| __Key__ | com.openexchange.mail.transport.publishingPublicInfostoreFolder |
|:----------------|:--------|
| __Description__ | Specify the name of the publishing infostore folder which is created below public infostore folder.<br>The denoted folder is created if absent only if "com.openexchange.mail.transport.enablePublishOnExceededQuota" is enabled.<br>The special identifier "i18n-defined" indicates to use translation of text "E-Mail attachments".<br> |
| __Default__ | i18n-defined |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.mail.transport.enablePublishOnExceededQuota |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Transport.html">Transport</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Infostore.html">Infostore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | transport.properties |

---
| __Key__ | com.openexchange.mail.transport.removeMimeVersionInSubParts |
|:----------------|:--------|
| __Description__ | Specify whether to strictly obey suggestion in RFC 2045.<br>The MIME-Version header field is required at the top level of a message, but is _not_ required for each body part of a multipart entity.<br>If set to "true", each message is processed to not contain a MIME-Version header in sub-parts.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Transport.html">Transport</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | transport.properties |

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
