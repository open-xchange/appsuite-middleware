---
title: Mail Account
---

This page shows all properties with the tag: Mail Account

| __Key__ | com.openexchange.mail.account.blacklist |
|:----------------|:--------|
| __Description__ | Specifies a black-list for those hosts that are covered by denoted IP range; e.g. "127.0.0.1-127.255.255.255, localhost, internal.domain.org"<br>Creation of mail accounts with this hosts will be prevented. Also the validation of those accounts will fail.<br>An empty value means no black-listing is active<br>Default is "127.0.0.1-127.255.255.255,localhost"<br> |
| __Default__ | 127.0.0.1-127.255.255.255,localhost |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Account.html">Mail Account</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.account.whitelist.ports |
|:----------------|:--------|
| __Description__ | Specifies a white-list for such ports that are allowed to connect against when setting up/validating an external mail account<br>An empty value means no white-listing is active<br>Default is: 143,993, 25,465,587, 110,995<br> |
| __Default__ | 143,993, 25,465,587, 110,995 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Account.html">Mail Account</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/White_List.html">White List</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mailaccount.failedAuth.limit |
|:----------------|:--------|
| __Description__ | Specifies the max. number of failed authentication attempts until the associated mail account is disabled.<br> |
| __Default__ | 5 |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mailaccount.failedAuth.span |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Account.html">Mail Account</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | mailaccount.properties |

---
| __Key__ | com.openexchange.mailaccount.failedAuth.span |
|:----------------|:--------|
| __Description__ | Specifies the time span in which the failed authentication attempts are tracked.<br>The value accepts known time span syntax like "1W" or "5m"<br> |
| __Default__ | 30m |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mailaccount.failedAuth.limit |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Account.html">Mail Account</a> |
| __File__ | mailaccount.properties |

---
