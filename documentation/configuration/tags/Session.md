---
title: Session
---

This page shows all properties with the tag: Session

| __Key__ | com.openexchange.http.grizzly.sessionExpiryCheckInterval |
|:----------------|:--------|
| __Description__ | Specifies the interval in seconds when to check for expired/invalid HTTP sessions<br>This value should be aligned to property "com.openexchange.servlet.maxInactiveInterval"<br>that defines how long (in seconds) a HTTP session may stay idle/inactive until considered<br>as invalid<br> |
| __Default__ | 60 |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Session.html">Session</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.ipcheck.mode |
|:----------------|:--------|
| __Description__ | Specifies the IP check mechanisms to apply.<br>Known values are: none, strict and countrycode<br>"none" implies that no IP check takes place at all<br>"strict" implies that IP addresses are checked for equality<br>"countrycode" requires open-xchange-geoip being installed and performs a plausibility check against IP addresses' country codes<br>Note: The "com.openexchange.IPCheck" property still has precedence over this property;<br>i.e. if "com.openexchange.IPCheck" is set to "true", strict IP check is enabled.<br>Default is empty<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.IPCheck |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Session.html">Session</a> |
| __File__ | server.properties |

---
