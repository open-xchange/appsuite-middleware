---
title: Filestorage
---

Pre-Configured file storages

Property:
"com.openexchange.file.storage.account." + <account-id> + "." + <prop-name> + "=" + <value>

Config option:
"com.openexchange.file.storage.account." + <account-id> + ".config." + <config-option> + "=" + <value>

Example WebDAV configuration
com.openexchange.file.storage.account.webdav.serviceId=com.openexchange.file.storage.webdav
com.openexchange.file.storage.account.webdav.displayName="WebDAV"
com.openexchange.file.storage.account.webdav.config.url=http://your-webdav-server
com.openexchange.file.storage.account.webdav.config.timeout=60000


| __Key__ | com.openexchange.file.storage.numberOfPregeneratedPreviews |
|:----------------|:--------|
| __Description__ | <br>Specifies the number of listed files in a Drive folder for which a preview/thumbnail is supposed to be pre-generated asynchronously.<br> |
| __Default__ | 20 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestorage.html">Filestorage</a> |
| __File__ | filestorage.properties |

---
