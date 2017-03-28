---
title: CalDAV
---

| __Key__ | com.openexchange.caldav.enabled |
|:----------------|:--------|
| __Description__ | Whether CalDAV is enabled or not<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CalDAV.html">CalDAV</a> |
| __File__ | caldav.properties |

---
| __Key__ | com.openexchange.caldav.tree |
|:----------------|:--------|
| __Description__ | Configures the ID of the folder tree used by the CalDAV interface.<br> |
| __Default__ | 0 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CalDAV.html">CalDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | caldav.properties |

---
| __Key__ | com.openexchange.caldav.interval.start |
|:----------------|:--------|
| __Description__ | Appointments and tasks are available via the CalDAV interface if they fall <br>into a configurable timeframe. This value specifies the start time of this <br>interval, i.e. how far past appointments should be considered. More formal, <br>this value defines the negative offset relative to the current date <br>representing the minimum end time of appointments to be synchronized.<br>Possible values are "one_month", "one_year" and "six_months". <br> |
| __Default__ | one_month |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CalDAV.html">CalDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a> |
| __File__ | caldav.properties |

---
| __Key__ | com.openexchange.caldav.interval.end |
|:----------------|:--------|
| __Description__ | Appointments and tasks are available via the CalDAV interface if they fall <br>into a configurable timeframe. This value specifies the end time of this<br>interval, i.e. how far future appointments should be considered. More <br>formal, this value defines the positive offset relative to the current date <br>representing the maximum start time of appointments to be synchronized.<br>Possible values are "one_year" and "two_years".<br> |
| __Default__ | one_year |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CalDAV.html">CalDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a> |
| __File__ | caldav.properties |

---
| __Key__ | com.openexchange.caldav.url |
|:----------------|:--------|
| __Description__ | Tells users where to find a caldav folder. This can be displayed in frontends.<br>You can use the variables [hostname] and [folderId] <br>If you chose to deploy caldav as a virtual host (say 'dav.open-xchange.com') use<br>https://dav.open-xchange.com/caldav/[folderId] as the value<br>If you are using user-agent sniffing use<br>https://[hostname]/caldav/[folderId]        <br> |
| __Default__ | https://[hostname]/caldav/[folderId] |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CalDAV.html">CalDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | caldav.properties |

---
| __Key__ | com.openexchange.caldav.push.apsd.enabled |
|:----------------|:--------|
| __Description__ | Enables or disables push event notifications using the Apple Push Notification service (APNS), targeting the Apple Calendar client on iOS and mac OS. This requires a valid configuration for the APNS certificate and keys, see options below.<br> |
| __Default__ | false |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CalDAV.html">CalDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | caldav.properties |

---
| __Key__ | com.openexchange.caldav.push.apsd.bundleId |
|:----------------|:--------|
| __Description__ | Defines the bundle identifier referring to the Apple Push "topic", which is extracted from the UID portion of the subject of the certificate acquired from Apple, e.g. "com.apple.calendar.XServer.934668ca-125e-4246-afee-8cf2df37aab8". <br>Required if com.openexchange.caldav.push.apsd.enabled is "true".<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.caldav.push.apsd.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CalDAV.html">CalDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | caldav.properties |

---
| __Key__ | com.openexchange.caldav.push.apsd.keystore |
|:----------------|:--------|
| __Description__ | Specifies the path to the local keystore file (PKCS #12) containing the APNS certificate and keys to use, e.g. "/opt/open-xchange/etc/com.apple.servermgrd.apns.calendar.p12". <br>Required if com.openexchange.caldav.push.apsd.enabled is "true".   <br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.caldav.push.apsd.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CalDAV.html">CalDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | caldav.properties |

---
| __Key__ | com.openexchange.caldav.push.apsd.password |
|:----------------|:--------|
| __Description__ | Specifies the password used when creating the referenced keystore containing the APNS certificate.  <br>Required if com.openexchange.caldav.push.apsd.enabled is "true".   <br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.caldav.push.apsd.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CalDAV.html">CalDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | caldav.properties |

---
| __Key__ | com.openexchange.caldav.push.apsd.production |
|:----------------|:--------|
| __Description__ | Indicates which APNS service is used when sending push notifications. A value of "true" will use the production service, a value of "false" the sandbox service. <br> |
| __Default__ | true |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CalDAV.html">CalDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | caldav.properties |

---
| __Key__ | com.openexchange.caldav.push.apsd.refreshInterval |
|:----------------|:--------|
| __Description__ | Defines a timespan (in seconds) that is advertised to clients to indicate how often they should refresh their push subscriptions. <br> |
| __Default__ | 172800 |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CalDAV.html">CalDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | caldav.properties |

---
