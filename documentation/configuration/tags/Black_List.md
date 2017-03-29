---
title: Black List
---

This page shows all properties with the tag: Black List

| __Key__ | com.openexchange.drive.excludedFolders |
|:----------------|:--------|
| __Description__ | Allows to exclude specific root folders from OX Drive synchronization explicitly. <br>Excluded folders may not be used as root folder for the synchronization, <br>however, this does not apply to their subfolders automatically.<br>Excluded folders should be specified in a comma-separated list of their unique identifiers.<br>Typical candidates for the blacklist would be folder 15 (the "public folders" root) <br>or folder 10 (the "shared folders" root) in large enterprise installations.      <br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.mail.account.blacklist |
|:----------------|:--------|
| __Description__ | Specifies a black-list for those hosts that are covered by denoted IP range; e.g. "127.0.0.1-127.255.255.255, localhost, internal.domain.org"<br>Creation of mail accounts with this hosts will be prevented. Also the validation of those accounts will fail.<br>An empty value means no black-listing is active<br>Default is "127.0.0.1-127.255.255.255,localhost"<br> |
| __Default__ | 127.0.0.1-127.255.255.255,localhost |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Account.html">Mail Account</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.messaging.rss.feed.blacklist |
|:----------------|:--------|
| __Description__ | Specifies a black-list for those hosts that are covered by denoted IP range; e.g. "127.0.0.1-127.255.255.255, localhost, internal.domain.org"<br>An empty value means no black-listing is active.<br> |
| __Default__ | 127.0.0.1-127.255.255.255,localhost |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/RSS.html">RSS</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a> |
| __File__ | rssmessaging.properties |

---
| __Key__ | com.openexchange.filestore.sproxyd.[filestoreID].connectionTimeout |
|:----------------|:--------|
| __Description__ | The connection timeout in milliseconds. If establishing a new HTTP connection to a certain<br>host, it is blacklisted until it is considered available again. A periodic heartbeat task<br>that tries to read the namespace configuration (<protocol>://<host>/<path>/.conf) decides<br>whether an endpoint is considered available again.<br> |
| __Default__ | 5000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | filestore-sproxyd.properties |

---
| __Key__ | com.openexchange.filestore.sproxyd.[filestoreID].socketReadTimeout |
|:----------------|:--------|
| __Description__ | The socket read timeout in milliseconds. If waiting for the next expected TCP packet exceeds<br>this value, the host is blacklisted until it is considered available again. A periodic heartbeat<br>task that tries to read the namespace configuration (<protocol>://<host>/<path>/.conf) decides<br>whether an endpoint is considered available again.<br> |
| __Default__ | 15000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | filestore-sproxyd.properties |

---
| __Key__ | com.openexchange.filestore.sproxyd.[filestoreID].heartbeatInterval |
|:----------------|:--------|
| __Description__ | Hosts can get blacklisted if the client consieders them to be unavailable. All hosts on the<br>blacklist are checked periodically if they are available again and are then removed from the<br>blacklist if so. A host is considered available again if the namespace configuration file<br>(<protocol>://<host>/<path>/.conf) can be requested without any error. This setting specifies<br>the interval in milliseconds between two heartbeat runs. The above specified timeouts must be<br>taken into account for specifying a decent value, as every heartbeat run might block until a<br>timeout happens for every still unavailable host.<br> |
| __Default__ | 60000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a> |
| __File__ | filestore-sproxyd.properties |

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
| __Key__ | com.openexchange.filestore.swift.[filestoreID].connectionTimeout |
|:----------------|:--------|
| __Description__ | The connection timeout in milliseconds. If establishing a new HTTP connection to a certain<br>host, it is blacklisted until it is considered available again. A periodic heartbeat task<br>that tries to read the namespace configuration (<protocol>://<host>/<path>/.conf) decides<br>whether an endpoint is considered available again.<br> |
| __Default__ | 5000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].socketReadTimeout |
|:----------------|:--------|
| __Description__ | The socket read timeout in milliseconds. If waiting for the next expected TCP packet exceeds<br>this value, the host is blacklisted until it is considered available again. A periodic heartbeat<br>task that tries to read the namespace configuration (<protocol>://<host>/<path>/.conf) decides<br>whether an endpoint is considered available again.<br> |
| __Default__ | 15000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].heartbeatInterval |
|:----------------|:--------|
| __Description__ | Hosts can get blacklisted if the client considers them to be unavailable. All hosts on the<br>blacklist are checked periodically if they are available again and are then removed from the<br>blacklist if so. A host is considered available again if the namespace configuration file<br>(<protocol>://<host>/<path>/.conf) can be requested without any error. This setting specifies<br>the interval in milliseconds between two heartbeat runs. The above specified timeouts must be<br>taken into account for specifying a decent value, as every heartbeat run might block until a<br>timeout happens for every still unavailable host.<br> |
| __Default__ | 60000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a> |
| __File__ | filestore-swift.properties |

---
