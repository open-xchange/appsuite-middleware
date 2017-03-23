---
title: Caching
---

Properties for com.openexchange.caching bundle


| __Key__ | com.openexchange.caching.jcs.enabled |
|:----------------|:--------|
| __Description__ | Specify whether JCS-based caching should be enabled.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a> |
| __File__ | cache.properties |

---
| __Key__ | com.openexchange.caching.jcs.eventInvalidation |
|:----------------|:--------|
| __Description__ | Configures how remote cache invalidation is done. Set to 'true' for cache events via the cache event messaging service, or to 'false'<br>to stick with the JCS-internal lateral auxiliary cache configuration.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a> |
| __File__ | cache.properties |

---
| __Key__ | com.openexchange.caching.jcs.remoteInvalidationForPersonalFolders |
|:----------------|:--------|
| __Description__ | Specifies if changes to personal folders (personal in terms of non-global e.g. folders kept in database) are supposed to be propagated<br>to remote nodes. This option is only useful for installations that do offer collaboration features or do not support session stickyness.<br>For instance users are able to share mail folders or might be load-balanced to other nodes while active in a single session.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Session.html">Session</a> |
| __File__ | cache.properties |

---
