---
title: CardDAV
---

| __Key__ | com.openexchange.carddav.enabled |
|:----------------|:--------|
| __Description__ | Whether CardDAV is enabled or not<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CardDAV.html">CardDAV</a> |
| __File__ | carddav.properties |

---
| __Key__ | com.openexchange.carddav.ignoreFolders |
|:----------------|:--------|
| __Description__ | A comma-separated list of folder IDs to exclude from the synchronization. <br>Use this to disable syncing of very large folders (e.g. the global address <br>list in large contexts, which always has ID 6). By default, no folders are<br>excluded.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CardDAV.html">CardDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | carddav.properties |

---
| __Key__ | com.openexchange.carddav.tree |
|:----------------|:--------|
| __Description__ | Configures the ID of the folder tree used by the CardDAV interface.<br> |
| __Default__ | 0 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CardDAV.html">CardDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | carddav.properties |

---
| __Key__ | com.openexchange.carddav.exposedCollections |
|:----------------|:--------|
| __Description__ | Controls which collections are exposed via the CardDAV interface. Possible <br>values are '0', '1' and '2'. A value of '1' makes each visible folder <br>available as a resource collection, while '2' only exposes an aggregated <br>collection containing  all contact resources from all visible folders. The <br>default value '0' exposes either an aggregated collection or individual <br>collections for each folder, depending on the client's user-agent that is <br>matched against the pattern in 'userAgentForAggregatedCollection'. <br> |
| __Default__ | 0 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CardDAV.html">CardDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | carddav.properties |

---
| __Key__ | com.openexchange.carddav.userAgentForAggregatedCollection |
|:----------------|:--------|
| __Description__ | Regular expression to match against the client's user-agent to decide <br>whether the aggregated collection is exposed or not. The default pattern <br>matches all known varieties of the Mac OS Addressbook client, that doesn't <br>support multiple collections. Only used if 'exposedCollections' is set to <br>'0'. The pattern is used case insensitive. <br> |
| __Default__ | .\*CFNetwork.\*Darwin.\*&#124;.\*AddressBook.\*CardDAVPlugin.\*Mac_OS_X.\*&#124;.\*Mac OS X.\*AddressBook.\* |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.carddav.exposedCollections |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CardDAV.html">CardDAV</a> |
| __File__ | carddav.properties |

---
| __Key__ | com.openexchange.carddav.userAgentForAggregatedCollection |
|:----------------|:--------|
| __Description__ | Specifies if all visible folders are used to create the aggregated <br>collection, or if a reduced set of folders only containing the global <br>addressbook and the personal contacts folders should be used. This setting<br>only influences the aggregated collection that is used for clients that<br>don't support multiple collections. Possible values are 'true' and 'false.<br> |
| __Default__ | .\*CFNetwork.\*Darwin.\*&#124;.\*AddressBook.\*CardDAVPlugin.\*Mac_OS_X.\*&#124;.\*Mac OS X.\*AddressBook.\* |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.carddav.exposedCollections |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CardDAV.html">CardDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | carddav.properties |

---
| __Key__ | com.openexchange.carddav.push.apsd.enabled |
|:----------------|:--------|
| __Description__ | Enables or disables push event notifications using the Apple Push Notification service (APNS), targeting the Apple Contacts client on iOS and mac OS. This requires a valid configuration for the APNS certificate and keys, see options below.<br> |
| __Default__ | false |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CardDAV.html">CardDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | carddav.properties |

---
| __Key__ | com.openexchange.carddav.push.apsd.bundleId |
|:----------------|:--------|
| __Description__ | Defines the bundle identifier referring to the Apple Push "topic", which is extracted from the UID portion of the subject of the certificate acquired from Apple, e.g. "com.apple.contact.XServer.a5243d3e-b635-11e6-80f5-76304dec7eb7". <br>Required if com.openexchange.carddav.push.apsd.enabled is "true".<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.carddav.push.apsd.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CardDAV.html">CardDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | carddav.properties |

---
| __Key__ | com.openexchange.carddav.push.apsd.keystore |
|:----------------|:--------|
| __Description__ | Specifies the path to the local keystore file (PKCS #12) containing the APNS certificate and keys to use, e.g. "/opt/open-xchange/etc/com.apple.servermgrd.apns.contact.p12". <br>Required if com.openexchange.carddav.push.apsd.enabled is "true".   <br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.carddav.push.apsd.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CardDAV.html">CardDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | carddav.properties |

---
| __Key__ | com.openexchange.carddav.push.apsd.password |
|:----------------|:--------|
| __Description__ | Specifies the password used when creating the referenced keystore containing the APNS certificate.  <br>Required if com.openexchange.carddav.push.apsd.enabled is "true".   <br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.carddav.push.apsd.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CardDAV.html">CardDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | carddav.properties |

---
| __Key__ | com.openexchange.carddav.push.apsd.production |
|:----------------|:--------|
| __Description__ | Indicates which APNS service is used when sending push notifications. A value of "true" will use the production service, a value of "false" the sandbox service. <br> |
| __Default__ | true |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CardDAV.html">CardDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | carddav.properties |

---
| __Key__ | com.openexchange.carddav.push.apsd.refreshInterval |
|:----------------|:--------|
| __Description__ | Defines a timespan (in seconds) that is advertised to clients to indicate how often they should refresh their push subscriptions. <br> |
| __Default__ | 172800 |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CardDAV.html">CardDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | carddav.properties |

---
