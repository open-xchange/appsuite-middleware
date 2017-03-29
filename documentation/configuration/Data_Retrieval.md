---
title: Data Retrieval
---

Configuration of the data retrieval component allowing external applications getting data from OX.


| __Key__ | com.openexchange.groupware.dataRetrieval.lifetime |
|:----------------|:--------|
| __Description__ | Lifetime of a token in milliseconds. If this value is not set or negative the token only expires at the end of the session.<br> |
| __Default__ | 60000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Data_Retrieval.html">Data Retrieval</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Session.html">Session</a> |
| __File__ | dataRetrieval.properties |

---
| __Key__ | com.openexchange.groupware.dataRetrieval.onetime |
|:----------------|:--------|
| __Description__ | Whether the token expires after the first access or not.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Data_Retrieval.html">Data Retrieval</a> |
| __File__ | dataRetrieval.properties |

---
| __Key__ | com.openexchange.groupware.dataRetrieval.forceProtocol |
|:----------------|:--------|
| __Description__ | Force use of a certain protocol for the retrieval URLs. Protocol defaults to the one used to access the backend.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Data_Retrieval.html">Data Retrieval</a> |
| __File__ | dataRetrieval.properties |

---
