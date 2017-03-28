---
title: Caching
---

This page shows all properties with the tag: Caching

| __Key__ | com.openexchange.imap[.primary].allowFolderCaches |
|:----------------|:--------|
| __Description__ | Enables/disables caching of IMAP folders.<br>Default is true.<br>Note: Only disable IMAP folder cache if you certainly know what you are doing.<br>Disabling that cache may result in noticeable performance decrease.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a> |
| __File__ | imap.properties |

---
| __Key__ | ENABLE_DB_GROUPING |
|:----------------|:--------|
| __Description__ | Define where to perform folder grouping.<br>This filters the database results for duplicate folders in the where <br>clause of the db statement or afterwards in the application. <br>Possible values: TRUE / FALSE<br> |
| __Default__ | TRUE |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a> |
| __File__ | foldercache.properties |

---
| __Key__ | ENABLE_FOLDER_CACHE |
|:----------------|:--------|
| __Description__ | Enable or disable folder caching. Possible values: TRUE / FALSE<br> |
| __Default__ | TRUE |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a> |
| __File__ | foldercache.properties |

---
| __Key__ | IGNORE_SHARED_ADDRESSBOOK |
|:----------------|:--------|
| __Description__ | Determine whether to ignore 'shared addressbook' folder or not.<br>Possible values: TRUE / FALSE<br> |
| __Default__ | TRUE |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | foldercache.properties |

---
| __Key__ | com.openexchange.preview.cache.enabled |
|:----------------|:--------|
| __Description__ | The switch to enable/disable the preview cache<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Preview.html">Preview</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a> |
| __File__ | preview.properties |

---
| __Key__ | com.openexchange.preview.cache.quota |
|:----------------|:--------|
| __Description__ | Specify the total quota for preview cache for each context<br>This value is used if no individual context quota is defined.<br>A value of zero or less means no quota<br> |
| __Default__ | 10485760 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Preview.html">Preview</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a> |
| __File__ | preview.properties |

---
| __Key__ | com.openexchange.preview.cache.quotaPerDocument |
|:----------------|:--------|
| __Description__ | Specify the quota per document for preview cache for each context<br>This value is used if no individual quota per document is defined.<br>A value of zero or less means no quota<br> |
| __Default__ | 524288 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Preview.html">Preview</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a> |
| __File__ | preview.properties |

---
| __Key__ | com.openexchange.preview.cache.type |
|:----------------|:--------|
| __Description__ | Specifies what type of storage is used for caching previews<br>Either file store ("FS") or database ("DB").<br> |
| __Default__ | FS |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Preview.html">Preview</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a> |
| __File__ | preview.properties |

---
| __Key__ | com.openexchange.preview.cache.quotaAware |
|:----------------|:--------|
| __Description__ | Specifies if storing previews in file store affects user's file store quota or not<br>Only applies if "com.openexchange.preview.cache.type" is set to "FS"<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Preview.html">Preview</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a> |
| __File__ | preview.properties |

---
| __Key__ | mail.imap.statuscachetimeout |
|:----------------|:--------|
| __Description__ | Timeout value in milliseconds for cache of STATUS command response. Default is 1000 (1 second). Zero disables cache.<br> |
| __Default__ | 1000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.store.maildir.cachefolders |
|:----------------|:--------|
| __Description__ | Whether to cache maildir folder objects or not.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.store.maildir.headercachemaxsize |
|:----------------|:--------|
| __Description__ | Define the maximum number of message headers that are kept in a maildir folder's header cache.<br> |
| __Default__ | 1000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.store.maildir.maxnumofheadercaches |
|:----------------|:--------|
| __Description__ | Define the maximum number of maildir folder header caches.<br> |
| __Default__ | 10 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.store.maildir.headercacheexpire |
|:----------------|:--------|
| __Description__ | Define the timeout for header caches (in milliseconds).<br> |
| __Default__ | 3600000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | javamail.properties |

---
| __Key__ | com.openexchange.caching.configfile |
|:----------------|:--------|
| __Description__ | Location of default cache configuration file<br> |
| __Default__ | cache.ccf |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/System.html">System</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a> |
| __File__ | system.properties |

---
| __Key__ | Cache |
|:----------------|:--------|
| __Description__ | Switch for enabling caching in the groupware. Normally this should be set to<br>true or the database will get a lot more load.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/System.html">System</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a> |
| __File__ | system.properties |

---
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
