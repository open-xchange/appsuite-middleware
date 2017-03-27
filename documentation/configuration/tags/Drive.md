---
title: Drive
---

This page shows all properties with the tag: Drive

| __Key__ | com.openexchange.mail.maxDriveAttachments |
|:----------------|:--------|
| __Description__ | Specifies the max. number of Drive documents that are allowed to be sent via E-Mail<br> |
| __Default__ | 20 |
| __Version__ | 7.6.2 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.compose.share.enabled |
|:----------------|:--------|
| __Description__ | The main switch to enable/disable to send composed share messages<br>Note: In order to effectively enable composed share messages, the "infostore" and "share_links" capabilities need also to be available<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a> |
| __File__ | mail-compose.properties |

---
| __Key__ | com.openexchange.mail.compose.share.name |
|:----------------|:--------|
| __Description__ | Specifies the naming for the feature to send composed share messages<br> |
| __Default__ | Drive Mail |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a> |
| __File__ | mail-compose.properties |

---
| __Key__ | com.openexchange.mail.compose.share.threshold |
|:----------------|:--------|
| __Description__ | Specifies the threshold in bytes when the client is supposed to send a share compose message.<br>Setting this option to 0 (zero) disables "forced" switch to a share compose message.<br> |
| __Default__ | 0 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a> |
| __File__ | mail-compose.properties |

---
| __Key__ | com.openexchange.mail.compose.share.externalRecipientsLocale |
|:----------------|:--------|
| __Description__ | Defines the locale to use when sending a composed share message to an external recipient.<br>Expects a locale identifier compliant to RFC 2798 and 2068; such as "en_US".<br>Special value "user-defined" means to select the sending user's locale.<br> |
| __Default__ | user-defined |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a> |
| __File__ | mail-compose.properties |

---
| __Key__ | com.openexchange.mail.compose.share.requiredExpiration |
|:----------------|:--------|
| __Description__ | Defines whether an expiration date is required to be set, which applied to<br>the folder/files that were shared via a share compose message.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a> |
| __File__ | mail-compose.properties |

---
| __Key__ | com.openexchange.mail.compose.share.forceAutoDelete |
|:----------------|:--------|
| __Description__ | Defines whether shared folder/files get automatically cleansed if an expiration date is exceeded<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a> |
| __File__ | mail-compose.properties |

---
| __Key__ | com.openexchange.mail.compose.share.preview.timeout |
|:----------------|:--------|
| __Description__ | Defines default timeout in milliseconds for preview image creation.<br> |
| __Default__ | 1000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a> |
| __File__ | mail-compose.properties |

---
| __Key__ | com.openexchange.mail.compose.share.documentPreviewEnabled |
|:----------------|:--------|
| __Description__ | If set to true, preview images for documents are generated. Needs readerengine.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a> |
| __File__ | mail-compose.properties |

---
| __Key__ | com.openexchange.client.onboarding.driveapp.store.google.playstore |
|:----------------|:--------|
| __Description__ | Specifies the URL to Google Play Store for the Drive App.<br> |
| __Default__ | https://play.google.com/store/apps/details?id=com.openexchange.drive.vanilla |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Android.html">Android</a> |
| __File__ | client-onboarding-driveapp.properties |

---
| __Key__ | com.openexchange.client.onboarding.driveapp.store.apple.appstore |
|:----------------|:--------|
| __Description__ | Specifies the URL to Apple App Store for the Drive App.<br> |
| __Default__ | https://itunes.apple.com/de/app/ox-drive/id798570177 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Apple.html">Apple</a> |
| __File__ | client-onboarding-driveapp.properties |

---
| __Key__ | com.openexchange.client.onboarding.driveapp.store.apple.macappstore |
|:----------------|:--------|
| __Description__ | Specifies the URL to Apple Mac App Store for the Drive App.<br> |
| __Default__ | https://itunes.apple.com/de/app/ox-drive/id818195014 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Apple.html">Apple</a> |
| __File__ | client-onboarding-driveapp.properties |

---
| __Key__ | com.openexchange.capability.drive |
|:----------------|:--------|
| __Description__ | Enables or disables the "drive" module capability globally. The capability<br>can also be set more fine-grained via config cascade. Per default it is only<br>enabled for users that have the "infostore" permission set. This is configured<br>in /opt/open-xchange/etc/contextSets/drive.yml.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Capability.html">Capability</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.shortProductName |
|:----------------|:--------|
| __Description__ | Short product name as used in the version comment string inserted for drive<br>uploads, e.g. "Uploaded with OX Drive (Ottos Laptop)".<br> |
| __Default__ | OX Drive |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.useTempFolder |
|:----------------|:--------|
| __Description__ | Specifies whether the synchronization logic will make use of a folder named<br>".drive" below the root synchronization folder or not. If enabled, this<br>folder is used to store temporary uploads and removed files, which usually<br>leads to a better user experience since previously synchronized files can<br>be restored from there for example. If not, removed files are not kept, and<br>uploads are performed directly in the target folder.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.cleaner.interval |
|:----------------|:--------|
| __Description__ | Configures the interval between runs of the cleaner process for the<br>temporary ".drive" folder. A cleaner run is only initiated if the<br>synchronization is idle, i.e. the last synchronization resulted in no<br>actions to be performed, and the last run was before the configured<br>interval. The value can be defined using units of measurement: "D" (=days),<br>"W" (=weeks) and "H" (=hours).<br> |
| __Default__ | 1D |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.cleaner.maxAge |
|:----------------|:--------|
| __Description__ | Defines the maximum age of files and directories to be kept inside the<br>temporary ".drive" folder. Files or directories that were last modified<br>before the configured age are deleted during the next run of the cleaner<br>process. The value can be defined using units of measurement: "D" (=days),<br>"W" (=weeks) and "H" (=hours).<br> |
| __Default__ | 1D |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.checksum.cleaner.interval |
|:----------------|:--------|
| __Description__ | Defines the interval of a periodic background task that performs cleanup <br>operations for cached checksums in the database. The task is executed only <br>once per interval in the cluster, so this value should be equally defined <br>on each node.<br>The value can be defined using units of measurement: "D" (=days), <br>"W" (=weeks) and "H" (=hours) with a minimum of "1H" (one hour). <br>A value of "0" disables the periodic background task.<br> |
| __Default__ | 1D |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.checksum.cleaner.maxAge |
|:----------------|:--------|
| __Description__ | Defines the timespan after which an unused checksum should be removed from <br>the database cache.<br>The value can be defined using units of measurement: "D" (=days), <br>"W" (=weeks) and "H" (=hours) with a minimum of "1D" (one day). <br> |
| __Default__ | 4W |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.maxBandwidth |
|:----------------|:--------|
| __Description__ | Allows to limit the maximum used bandwidth for all downloads. If<br>configured, downloads via the drive module handled by this backend node will<br>not exceed the configured bandwidth. The available bandwidth is defined as<br>the number of allowed bytes per second, where the byte value can be<br>specified with one of the units "B" (bytes), "kB" (kilobyte), "MB"<br>(Megabyte) or "GB" (Gigabyte), e.g. "10 MB". Must fit into the "Integer"<br>range, i.e. the configured number of bytes has to be be smaller than 2^31.<br>"-1" means no limitations.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.maxBandwidthPerClient |
|:----------------|:--------|
| __Description__ | Allows to limit the maximum used bandwidth for client downloads within the<br>same session. If configured, downloads originating in the same session via<br>the drive module handled by this backend node will not exceed the<br>configured bandwidth. The available bandwidth is defined as the number of<br>allowed bytes per second, where the byte value can be specified with one of<br>the units "B" (bytes), "kB" (kilobyte), "MB" (Megabyte) or "GB" (Gigabyte),<br>e.g. "500 kB". Must fit into the "Integer" range, i.e. the configured<br>number of bytes has to be be smaller than 2^31. <br>"-1" means no limitations.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.maxConcurrentSyncOperations |
|:----------------|:--------|
| __Description__ | Specifies the maximum allowed number of synchronization operations, i.e.<br>all requests to the "drive" module apart from up- and downloads, that the<br>server accepts concurrently. While the limit is reached, further<br>synchronization requests are rejected in a HTTP 503 manner (service<br>unavailable), and the client is instructed to try again at a later time.<br>"-1" means no limitations.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.maxDirectories |
|:----------------|:--------|
| __Description__ | Defines the maximum number of synchronizable directories per root folder. A<br>value of "-1" disables the limitation.<br> |
| __Default__ | 65535 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.maxFilesPerDirectory |
|:----------------|:--------|
| __Description__ | Defines the maximum number of synchronizable files per root folder. A<br>value of "-1" disables the limitation.<br> |
| __Default__ | 65535 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.enabledServices |
|:----------------|:--------|
| __Description__ | Configures a list of allowed file storage services where synchronization via OX Drive should be enabled. <br>The services must be defined in a comma-separated list of their unique identifiers.<br> |
| __Default__ | com.openexchange.infostore |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.excludedFolders |
|:----------------|:--------|
| __Description__ | Allows to exclude specific root folders from OX Drive synchronization explicitly. <br>Excluded folders may not be used as root folder for the synchronization, <br>however, this does not apply to their subfolders automatically.<br>Excluded folders should be specified in a comma-separated list of their unique identifiers.<br>Typical candidates for the blacklist would be folder 15 (the "public folders" root) <br>or folder 10 (the "shared folders" root) in large enterprise installations.      <br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.directLinkQuota |
|:----------------|:--------|
| __Description__ | Configures the pattern for a direct link to manage a user's quota. <br>Text in brackets is replaced dynamically during link generation in the backend,<br>however, it's still possible to overwrite them here with a static value, or<br>even define an arbitrary URL here.<br>[protocol] is replaced automatically with the protocol used by the client<br>(typically "http" or "https").<br>[hostname] should be replaced with the server's canonical host name (if not,<br>the server tries to determine the hostname on it's own), <br>[uiwebpath] is replaced with the value of "com.openexchange.UIWebPath" as defined in<br>"server.properties", while [dispatcherPrefix] is replaced with the value of<br>"com.openexchange.dispatcher.prefix" ("server.properties", too).<br>[contextid], [userid] and [login] are replaced to reflect the values of the<br>current user.     <br> |
| __Default__ | [protocol]://[hostname] |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.directLinkHelp |
|:----------------|:--------|
| __Description__ | Configures the pattern for a direct link to the online help. This serves as<br>target for the "Help" section in the client applications. Text in brackets<br>is replaced dynamically during link generation in the backend, however, it's<br>still possible to overwrite them here with a static value, or even define an<br>arbitrary URL here.<br>[protocol] is replaced automatically with the protocol used by the client<br>(typically "http" or "https").<br>[hostname] should be replaced with the server's canonical host name (if not,<br>the server tries to determine the hostname on it's own), <br>[uiwebpath] is replaced with the value of "com.openexchange.UIWebPath" as defined in<br>"server.properties", while [dispatcherPrefix] is replaced with the value of<br>"com.openexchange.dispatcher.prefix" ("server.properties", too).<br>[contextid], [userid] and [login] are replaced to reflect the values of the<br>current user.   <br> |
| __Default__ | [protocol]://[hostname]/[uiwebpath]/help-drive/l10n/[locale]/index.html |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.events.apn.ios.enabled |
|:----------------|:--------|
| __Description__ | Enables or disables push event notifications to clients using the Apple Push<br>Notification service (APNS) for iOS devices. This requires a valid<br>configuration for the APNS certificate and keys, see either options below,<br>or install the restricted components packages for drive.  <br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Apple.html">Apple</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.events.apn.ios.keystore |
|:----------------|:--------|
| __Description__ | Specifies the path to the local keystore file (PKCS #12) containing the APNS<br>certificate and keys for the iOS application, e.g.<br>"/opt/open-xchange/etc/drive-apns.p12". Required if<br>"com.openexchange.drive.events.apn.ios.enabled" is "true" and the package<br>containing the restricted drive components is not installed.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.drive.events.apn.ios.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Apple.html">Apple</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.events.apn.ios.password |
|:----------------|:--------|
| __Description__ | Specifies the password used when creating the referenced keystore containing<br>the certificate of the iOS application. Note that blank or null passwords<br>are in violation of the PKCS #12 specifications. Required if<br>"com.openexchange.drive.events.apn.ios.enabled" is "true" and the package<br>containing the restricted drive components is not installed.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.drive.events.apn.ios.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Apple.html">Apple</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.events.apn.ios.production |
|:----------------|:--------|
| __Description__ | Indicates which APNS service is used when sending push notifications to iOS<br>devices. A value of "true" will use the production service, a value of<br>"false" the sandbox service.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Apple.html">Apple</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.events.apn.ios.feedbackQueryInterval |
|:----------------|:--------|
| __Description__ | Configures the interval between queries to the APN feedback service for the<br>subscribed iOS devices. The value can be defined using units of measurement:<br>"D" (=days), "W" (=weeks) and "H" (=hours).<br>Leaving this parameter empty disables the feedback queries on this node.<br>Since each received feedback is processed cluster-wide, only one node in the<br>cluster should be enabled here.<br> |
| __Default__ | 1D |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Apple.html">Apple</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.events.apn.macos.enabled |
|:----------------|:--------|
| __Description__ | Enables or disables push event notifications to clients using the Apple Push<br>Notification service (APNS) for Mac OS devices. This requires a valid<br>configuration for the APNS certificate and keys, see either options below,<br>or install the restricted components packages for drive.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Apple.html">Apple</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.events.apn.macos.keystore |
|:----------------|:--------|
| __Description__ | Specifies the path to the local keystore file (PKCS #12) containing the APNS<br>certificate and keys for the Mac OS application, e.g.<br>"/opt/open-xchange/etc/drive-apns.p12". Required if<br>"com.openexchange.drive.events.apn.macos.enabled" is "true" and the package<br>containing the restricted drive components is not installed.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.drive.events.apn.macos.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Apple.html">Apple</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.events.apn.macos.password |
|:----------------|:--------|
| __Description__ | Specifies the password used when creating the referenced keystore containing<br>the certificate of the Mac OS application. Note that blank or null passwords<br>are in violation of the PKCS #12 specifications. Required if<br>"com.openexchange.drive.events.apn.macos.enabled" is "true" and the package<br>containing the restricted drive components is not installed.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.drive.events.apn.macos.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Apple.html">Apple</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.events.apn.macos.production |
|:----------------|:--------|
| __Description__ | Indicates which APNS service is used when sending push notifications to Mac<br>OS devices. A value of "true" will use the production service, a value of<br>"false" the sandbox service.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.drive.events.apn.macos.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Apple.html">Apple</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.events.apn.macos.feedbackQueryInterval |
|:----------------|:--------|
| __Description__ | Configures the interval between queries to the APN feedback service for the<br>subscribed Mac OS devices. The value can be defined using units of<br>measurement: "D" (=days), "W" (=weeks) and "H" (=hours). <br>Leaving this parameter empty disables the feedback queries on<br>this node. Since each received feedback is processed cluster-wide, only one<br>node in the cluster should be enabled here.<br> |
| __Default__ | 1D |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.drive.events.apn.macos.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Apple.html">Apple</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.events.gcm.enabled |
|:----------------|:--------|
| __Description__ | Enables or disables push event notifications to clients using the Google<br>Cloud Messaging (GCM) service. This requires a valid configuration for the<br>GCM API key, see options below.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Android.html">Android</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.events.gcm.key |
|:----------------|:--------|
| __Description__ | Specifies the API key of the server application. Required if<br>"com.openexchange.drive.events.gcm.enabled" is "true" and the package<br>containing the restricted drive components is not installed.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.drive.events.gcm.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Android.html">Android</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.events.blockingLongPolling.enabled |
|:----------------|:--------|
| __Description__ | Configures whether blocking long polling for pushing synchronization events<br>to clients may be used as fallback when no other long polling handlers are<br>available due to missing support of the HTTP service. Handling long polling<br>in a blocking manner consumes a server thread, and should therefore only be<br>enabled for testing purposes. Defaults to "false".<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.version.[client].softMinimum |
|:----------------|:--------|
| __Description__ | This property allows the configuration of "soft" limit restrictions for<br>the supported clients. This limit has informational<br>character only, i.e. the client is just informed about an available update<br>when identifying with a lower version number. <br>The property is disabled by default to always fall back to the<br>recommended setting, but can be overridden if needed.<br>[client] must be replaced with one of the following:<br>  \* windows<br>  \* macos<br>  \* ios<br>  \* android<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Windows.html">Windows</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Android.html">Android</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Apple.html">Apple</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.version.[client].hardMinimum |
|:----------------|:--------|
| __Description__ | This property allows the configuration of "hard" limit restrictions for<br>the supported clients. This limit will restrict further synchronization <br>of clients that identify themselves with a lower version number.<br>The property is disabled by default to always fall back to the<br>recommended setting, but can be overridden if needed.<br>[client] must be replaced with one of the following:<br>  \* windows<br>  \* macos<br>  \* ios<br>  \* android<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Windows.html">Windows</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Android.html">Android</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Apple.html">Apple</a> |
| __File__ | drive.properties |

---
