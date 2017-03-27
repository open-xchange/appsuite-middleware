---
title: RSS
---

| __Key__ | com.openexchange.messaging.rss.feed.schemes |
|:----------------|:--------|
| __Description__ | Defines the URL schemes that are allowed while adding new RSS feeds. An empty value means all (by URL supported) schemes are allowed.<br> |
| __Default__ | http, https, ftp |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/RSS.html">RSS</a> |
| __File__ | rssmessaging.properties |

---
| __Key__ | com.openexchange.rss |
|:----------------|:--------|
| __Description__ | If set to false disables the rss capability.<br> |
| __Default__ | true |
| __Version__ | 7.2.1 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/RSS.html">RSS</a> |
| __File__ | rssmessaging.properties |

---
| __Key__ | com.openexchange.messaging.rss |
|:----------------|:--------|
| __Description__ | Whether the RSS Messaging Service is available or not.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/RSS.html">RSS</a> |
| __File__ | rssmessaging.properties |

---
| __Key__ | com.openexchange.messaging.rss.feed.size |
|:----------------|:--------|
| __Description__ | Defines the maximum feed size for an RSS feed in bytes.<br> |
| __Default__ | 4194304 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/RSS.html">RSS</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | rssmessaging.properties |

---
| __Key__ | com.openexchange.messaging.rss.feed.blacklist |
|:----------------|:--------|
| __Description__ | Specifies a black-list for those hosts that are covered by denoted IP range; e.g. "127.0.0.1-127.255.255.255, localhost, internal.domain.org"<br>An empty value means no black-listing is active.<br> |
| __Default__ | 127.0.0.1-127.255.255.255,localhost |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/RSS.html">RSS</a> |
| __File__ | rssmessaging.properties |

---
| __Key__ | com.openexchange.messaging.rss.feed.whitelist.ports |
|:----------------|:--------|
| __Description__ | Specifies a white-list for such ports that are allowed to connect against when adding/updating a RSS feed.<br>No port will always be accepted. <br>An empty value means no white-listing is active.<br> |
| __Default__ | 80,443 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/RSS.html">RSS</a> |
| __File__ | rssmessaging.properties |

---
