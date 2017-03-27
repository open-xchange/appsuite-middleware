---
title: Transport
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
