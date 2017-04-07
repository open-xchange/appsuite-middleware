---
title: Sms-tools
---

| __Key__ | com.openexchange.sms.userlimit.enabled |
|:----------------|:--------|
| __Description__ | Enables or disables the user sms limit<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Sms-tools.html">Sms-tools</a> |
| __File__ | sms-tools.properties |

---
| __Key__ | com.openexchange.sms.userlimit |
|:----------------|:--------|
| __Description__ | Defines the maximum number of sms messages a user can send<br> |
| __Default__ | 5 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Sms-tools.html">Sms-tools</a> |
| __File__ | sms-tools.properties |

---
| __Key__ | com.openexchange.sms.userlimit.refreshInterval |
|:----------------|:--------|
| __Description__ | Defines the time in minutes after that the user sms limits are refreshed.<br>For this value to work it must always be lower than the hazelcast eviction time. <br>Therefore if you change this value it is probably necessary to change the hazelcast eviction time too.<br> |
| __Default__ | 1440 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Sms-tools.html">Sms-tools</a> |
| __File__ | sms-tools.properties |

---
