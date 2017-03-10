---
title: IPCheck
---

| __Key__ | com.openexchange.ipcheck.mode |
|:----------------|:--------|
| __Description__ | Specifies the IP check mechanisms to apply.<br>Known values are: none, strict and countrycode<br>"none" implies that no IP check takes place at all<br>"strict" implies that IP addresses are checked for equality<br>"countrycode" requires open-xchange-geoip being installed and performs a plausibility check against IP addresses' country codes<br>Note: The "com.openexchange.IPCheck" property still has precedence over this property;<br>i.e. if "com.openexchange.IPCheck" is set to "true", strict IP check is enabled.<br>Default is empty<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.IPCheck |
| __File__ | server.properties |

---
