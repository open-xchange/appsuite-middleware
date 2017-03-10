---
title: PushNotificationService
---

| __Key__ | com.openexchange.pns.delayDuration |
|:----------------|:--------|
| __Description__ | The time in milliseconds a notification is queued in buffer to possible aggregate<br>with similar notifications that arrive during that time<br> |
| __Default__ | 1000 |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __File__ | pns.properties |

---
| __Key__ | com.openexchange.pns.timerFrequency |
|:----------------|:--------|
| __Description__ | The frequency/delay in milliseconds when the buffering queue will be checked for due<br>notifications (the ones exceeding delayDuration in queue)<br> |
| __Default__ | 500 |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __File__ | pns.properties |

---
| __Key__ | com.openexchange.pns.numProcessorThreads |
|:----------------|:--------|
| __Description__ | Specifies the number of threads that concurrently handle due notifications that were transferred<br>from buffering queue to processing queue.<br> |
| __Default__ | 500 |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __File__ | pns.properties |

---
| __Key__ | com.openexchange.pns.maxProcessorTasks |
|:----------------|:--------|
| __Description__ | Specifies the buffer size for due notifications that were transferred from buffering queue to<br>processing queue.<br> |
| __Default__ | 65536 |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __File__ | pns.properties |

---
| __Key__ | com.openexchange.pns.transport.apn.ios.enabled |
|:----------------|:--------|
| __Description__ | Specifies whether the APNS transport is enabled. That property is responsive to config-cascade<br>and reloadable as well.<br><br>Moreover, an even finer-grained decision is possible to be configured as a certain transport<br>is checked for availability providing user, context, client and topic.<br>Hence, it is possible to  specify:<br><br>com.openexchange.pns.transport.apn.ios.enabled + ("." + {client})? + ("." + {topic})?<br><br>com.openexchange.pns.transport.apn.ios.enabled.open-xchange-appsuite.ox:mail:new=true<br>com.openexchange.pns.transport.apn.ios.enabled.open-xchange-appsuite.ox:calendar:new=false<br><br>That allows the client "open-xchange-appsuite" (App Suite UI) to receive "new mail" notifications<br>via APNS, but not for "new appointment".<br> |
| __Default__ | false |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __File__ | pns.properties |

---
| __Key__ | com.openexchange.pns.transport.apn.ios.feedbackQueryInterval |
|:----------------|:--------|
| __Description__ | Specifies the frequency in milliseconds when to query the Apple feedback service to check for expired<br>and/or invalid tokens.<br> |
| __Default__ | 3600000 |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __File__ | pns.properties |

---
| __Key__ | com.openexchange.pns.transport.gcm.enabled |
|:----------------|:--------|
| __Description__ | Specifies whether the GCM transport is enabled. That property is responsive to config-cascade<br>and reloadable as well.<br><br>Moreover, an even finer-grained decision is possible to be configured as a certain transport<br>is checked for availability providing user, context, client and topic.<br>Hence, it is possible to  specify:<br><br>com.openexchange.pns.transport.gcm.enabled + ("." + {client})? + ("." + {topic})?<br><br>com.openexchange.pns.transport.gcm.enabled.open-xchange-appsuite.ox:mail:new=true<br>com.openexchange.pns.transport.gcm.enabled.open-xchange-appsuite.ox:calendar:new=false<br><br>That allows the client "open-xchange-appsuite" (App Suite UI) to receive "new mail" notifications<br>via GCM, but not for "new appointment".<br> |
| __Default__ | false |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __File__ | pns.properties |

---
| __Key__ | com.openexchange.pns.transport.wns.enabled |
|:----------------|:--------|
| __Description__ | Specifies whether the WNS transport is enabled. That property is responsive to config-cascade<br>and reloadable as well.<br><br>Moreover, an even finer-grained decision is possible to be configured as a certain transport<br>is checked for availability providing user, context, client and topic.<br>Hence, it is possible to  specify:<br><br>com.openexchange.pns.transport.wns.enabled + ("." + {client})? + ("." + {topic})?<br><br>com.openexchange.pns.transport.wns.enabled.open-xchange-appsuite.ox:mail:new=true<br>com.openexchange.pns.transport.wns.enabled.open-xchange-appsuite.ox:calendar:new=false<br><br>That allows the client "open-xchange-appsuite" (App Suite UI) to receive "new mail" notifications<br>via WNS, but not for "new appointment".<br> |
| __Default__ | false |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
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
| __File__ | pns.properties |

---
