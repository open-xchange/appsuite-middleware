---
title: Push
---

This page shows all properties with the tag: Push

| __Key__ | com.openexchange.pns.mobile.api.facade.apn.badge.enabled |
|:----------------|:--------|
| __Description__ | Specifies if badges are enabled when using push notifications for the OX Mail app for iOS.<br>These get displayed on the app icon.<br> |
| __Default__ | true |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | pns-mobile-api-facade.properties |

---
| __Key__ | com.openexchange.pns.mobile.api.facade.apn.sound.enabled |
|:----------------|:--------|
| __Description__ | Specifies if a sound should be played when the OX Mail app on iOS receives a push notification.<br> |
| __Default__ | true |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | pns-mobile-api-facade.properties |

---
| __Key__ | com.openexchange.pns.mobile.api.facade.apn.sound.filename |
|:----------------|:--------|
| __Description__ | Specifies the filename of the sound to play when a push notification is received in the OX Mail app on iOS.<br>This file needs to be included in the app, otherwise a default sound is played. the string 'default' also causes<br>the default iOS sound to be played.<br> |
| __Default__ | default |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | pns-mobile-api-facade.properties |

---
| __Key__ | com.openexchange.http.grizzly.hasCometEnabled |
|:----------------|:--------|
| __Description__ | Comet is an umbrella term used to describe a technique allowing web browser to<br>receive almost real time updates from the server. The two most common<br>approaches are long polling and streaming. Long polling differs from streaming<br>in that each update from the server ultimately results in another follow up<br>request from the client.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.http.grizzly.hasWebSocketsEnabled |
|:----------------|:--------|
| __Description__ | Bi-directional, full-duplex communications channels over a single TCP connection.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Websockets.html">Websockets</a> |
| __File__ | grizzly.properties |

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
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CalDAV.html">CalDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
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
| __Key__ | com.openexchange.pns.delayDuration |
|:----------------|:--------|
| __Description__ | The time in milliseconds a notification is queued in buffer to possible aggregate<br>with similar notifications that arrive during that time<br> |
| __Default__ | 1000 |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | pns.properties |

---
| __Key__ | com.openexchange.pns.timerFrequency |
|:----------------|:--------|
| __Description__ | The frequency/delay in milliseconds when the buffering queue will be checked for due<br>notifications (the ones exceeding delayDuration in queue)<br> |
| __Default__ | 500 |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | pns.properties |

---
| __Key__ | com.openexchange.pns.numProcessorThreads |
|:----------------|:--------|
| __Description__ | Specifies the number of threads that concurrently handle due notifications that were transferred<br>from buffering queue to processing queue.<br> |
| __Default__ | 500 |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | pns.properties |

---
| __Key__ | com.openexchange.pns.maxProcessorTasks |
|:----------------|:--------|
| __Description__ | Specifies the buffer size for due notifications that were transferred from buffering queue to<br>processing queue.<br> |
| __Default__ | 65536 |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | pns.properties |

---
| __Key__ | com.openexchange.pns.transport.apn.ios.enabled |
|:----------------|:--------|
| __Description__ | Specifies whether the APNS transport is enabled. That property is responsive to config-cascade<br>and reloadable as well.<br><br>Moreover, an even finer-grained decision is possible to be configured as a certain transport<br>is checked for availability providing user, context, client and topic.<br>Hence, it is possible to  specify:<br><br>com.openexchange.pns.transport.apn.ios.enabled + ("." + {client})? + ("." + {topic})?<br><br>com.openexchange.pns.transport.apn.ios.enabled.open-xchange-appsuite.ox:mail:new=true<br>com.openexchange.pns.transport.apn.ios.enabled.open-xchange-appsuite.ox:calendar:new=false<br><br>That allows the client "open-xchange-appsuite" (App Suite UI) to receive "new mail" notifications<br>via APNS, but not for "new appointment".<br> |
| __Default__ | false |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | pns.properties |

---
| __Key__ | com.openexchange.pns.transport.apn.ios.feedbackQueryInterval |
|:----------------|:--------|
| __Description__ | Specifies the frequency in milliseconds when to query the Apple feedback service to check for expired<br>and/or invalid tokens.<br> |
| __Default__ | 3600000 |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | pns.properties |

---
| __Key__ | com.openexchange.pns.transport.gcm.enabled |
|:----------------|:--------|
| __Description__ | Specifies whether the GCM transport is enabled. That property is responsive to config-cascade<br>and reloadable as well.<br><br>Moreover, an even finer-grained decision is possible to be configured as a certain transport<br>is checked for availability providing user, context, client and topic.<br>Hence, it is possible to  specify:<br><br>com.openexchange.pns.transport.gcm.enabled + ("." + {client})? + ("." + {topic})?<br><br>com.openexchange.pns.transport.gcm.enabled.open-xchange-appsuite.ox:mail:new=true<br>com.openexchange.pns.transport.gcm.enabled.open-xchange-appsuite.ox:calendar:new=false<br><br>That allows the client "open-xchange-appsuite" (App Suite UI) to receive "new mail" notifications<br>via GCM, but not for "new appointment".<br> |
| __Default__ | false |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | pns.properties |

---
| __Key__ | com.openexchange.pns.transport.wns.enabled |
|:----------------|:--------|
| __Description__ | Specifies whether the WNS transport is enabled. That property is responsive to config-cascade<br>and reloadable as well.<br><br>Moreover, an even finer-grained decision is possible to be configured as a certain transport<br>is checked for availability providing user, context, client and topic.<br>Hence, it is possible to  specify:<br><br>com.openexchange.pns.transport.wns.enabled + ("." + {client})? + ("." + {topic})?<br><br>com.openexchange.pns.transport.wns.enabled.open-xchange-appsuite.ox:mail:new=true<br>com.openexchange.pns.transport.wns.enabled.open-xchange-appsuite.ox:calendar:new=false<br><br>That allows the client "open-xchange-appsuite" (App Suite UI) to receive "new mail" notifications<br>via WNS, but not for "new appointment".<br> |
| __Default__ | false |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | pns.properties |

---
| __Key__ | com.openexchange.pns.transport.websocket.enabled |
|:----------------|:--------|
| __Description__ | Specifies whether the Web Socket transport is enabled. That property is responsive to config-cascade<br>and reloadable as well.<br><br>Moreover, an even finer-grained decision is possible to be configured as a certain transport<br>is checked for availability providing user, context, client and topic.<br>Hence, it is possible to  specify:<br><br>com.openexchange.pns.transport.websocket.enabled + ("." + {client})? + ("." + {topic})?<br><br>com.openexchange.pns.transport.websocket.enabled.open-xchange-appsuite.ox:mail:new=true<br>com.openexchange.pns.transport.websocket.enabled.open-xchange-appsuite.ox:calendar:new=false<br><br>That allows the client "open-xchange-appsuite" (App Suite UI) to receive "new mail" notifications<br>via Web Socket, but not for "new appointment".<br> |
| __Default__ | true |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.websockets.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | pns.properties |

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
