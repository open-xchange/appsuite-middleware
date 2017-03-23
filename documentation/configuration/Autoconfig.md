---
title: Autoconfig
---

| __Key__ | com.openexchange.mail.autoconfig.path |
|:----------------|:--------|
| __Description__ | Path to the local configuration files for mail domains.<br>See https://developer.mozilla.org/en/Thunderbird/Autoconfiguration<br> |
| __Default__ | /opt/open-xchange/ispdb |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Autoconfig.html">Autoconfig</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | autoconfig.properties |

---
| __Key__ | com.openexchange.mail.autoconfig.ispdb |
|:----------------|:--------|
| __Description__ | The ISPDB is a central database, currently hosted by Mozilla Messaging, but free to use for any client.<br>It contains settings for the world's largest ISPs.<br>We hope that the database will soon have enough information to autoconfigure approximately 50% of our user's email accounts.<br> |
| __Default__ | https://live.mozillamessaging.com/autoconfig/v1.1/ |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Autoconfig.html">Autoconfig</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | autoconfig.properties |

---
| __Key__ | com.openexchange.mail.autoconfig.http.proxy |
|:----------------|:--------|
| __Description__ | Provides the possibility to specify a proxy that is used to access any HTTP end-points. If empty, no proxy is used.<br>Notation is: <optional-protocol> + "://" + <proxy-host> + ":" + <proxy-port><br>             With "http" as fall-back protocol<br>E.g. "67.177.104.230:58720" (using HTTP protocol) or "https://78.0.25.45:8345" (using HTTPS protocol)<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Autoconfig.html">Autoconfig</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | autoconfig.properties |

---
| __Key__ | com.openexchange.mail.autoconfig.http.proxy.login |
|:----------------|:--------|
| __Description__ | Specifies the login/username to use in case specified proxy in property "com.openexchange.mail.autoconfig.http.proxy"<br>requires authentication.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mail.autoconfig.http.proxy.password, com.openexchange.mail.autoconfig.http.proxy |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Autoconfig.html">Autoconfig</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | autoconfig.properties |

---
| __Key__ | com.openexchange.mail.autoconfig.http.proxy.password |
|:----------------|:--------|
| __Description__ | Specifies the password to use in case specified proxy in property "com.openexchange.mail.autoconfig.http.proxy"<br>requires authentication.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mail.autoconfig.http.proxy.login, com.openexchange.mail.autoconfig.http.proxy |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Autoconfig.html">Autoconfig</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | autoconfig.properties |

---
| __Key__ | com.openexchange.mail.autoconfig.allowGuess |
|:----------------|:--------|
| __Description__ | Specifies whether it is allowed to "guess" the mail/transport settings.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Autoconfig.html">Autoconfig</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | autoconfig.properties |

---
