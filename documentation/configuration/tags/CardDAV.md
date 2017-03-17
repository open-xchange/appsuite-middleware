---
title: CardDAV
---

This page shows all properties with the tag: CardDAV

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
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CardDAV.html">CardDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
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
