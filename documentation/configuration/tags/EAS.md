---
title: EAS
---

This page shows all properties with the tag: EAS

| __Key__ | com.openexchange.client.onboarding.eas.url |
|:----------------|:--------|
| __Description__ | Specifies the URL to the EAS end-point; e.g. "eas.open-xchange.invalid" or "http://eas.open-xchange.invalid".<br><br>Note:<br>Specifying a protocol/scheme is optional and may be used to control whether the end-point is<br>supposed to be accessed via SSL or not.<br>Moreover, any path information is stripped off as only host name, port and SSL/No-SSL are relevant.<br>The administrator has ensure that end-point is reachable by a well-known path.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/EAS.html">EAS</a> |
| __File__ | client-onboarding-eas.properties |

---
| __Key__ | com.openexchange.client.onboarding.eas.login.customsource |
|:----------------|:--------|
| __Description__ | Specifies whether a look-up is supposed to be performed to check for custom login sources that might be registered<br>If available, then the login string is taken from such a custom login source<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/EAS.html">EAS</a> |
| __File__ | client-onboarding-eas.properties |

---
