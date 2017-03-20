---
title: Share
---

This page shows all properties with the tag: Share

| __Key__ | com.openexchange.share.notification.usePersonalEmailAddress |
|:----------------|:--------|
| __Description__ | Specifies whether the user's personal E-Mail address (true) or the configured no-reply address (false) is supposed to be used in case a user<br>without mail permission sends out a sharing invitation<br> |
| __Default__ | false |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | share.properties |

---
| __Key__ | com.openexchange.share.userAgentBlacklist.enabled |
|:----------------|:--------|
| __Description__ | Enables (default) or disables black-listing of User-Agents that are about to access a share link.<br>If enabled the "com.openexchange.share.userAgentBlacklist.values" configuration option specifies what User-Agents to black-list.<br> |
| __Default__ | true |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a> |
| __File__ | share.properties |

---
| __Key__ | com.openexchange.share.userAgentBlacklist.values |
|:----------------|:--------|
| __Description__ | Specifies a comma-separated list (ignore-case) of such User-Agents that are supposed to receive a "404 Not Found" when trying to<br>access a share link. White-card notation is supported; e.g. "\*aolbuild\*".<br>This configuration option is only effective if "com.openexchange.share.userAgentBlacklist.enabled" is set to "true"<br> |
| __Default__ | \*aolbuild\*, \*baiduspider\*, \*baidu\*search\*, \*bingbot\*, \*bingpreview\*, \*msnbot\*, \*duckduckgo\*, \*adsbot-google\*, \*googlebot\*, \*mediapartners-google\*, \*teoma\*, \*slurp\*, \*yandex\*bot\* |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.share.userAgentBlacklist.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a> |
| __File__ | share.properties |

---
