---
title: Push Notification Service
---

# Introduction

Starting with v7.8.3 the Open-Xchange Middleware implemented a generic Push Notification Service that supports delivering arbitrary
user-associated push notification messages to multiple clients in a transport-agnostic way.

Although designed to fit a general purpose approach, at first routing "new mail" notification messages via Web Sockets to the App Suite UI is the primary focus of the current implemenation.

## Installation

1. Install the "open-xchange-websockets-grizzly" package and follow the instructions from "Web Sockets" article
2. Install the "open-xchange-pns-impl" as well as "open-xchange-pns-transport-websockets" package
3. Enable the `com.openexchange.pns.transport.websocket.enabled` property.
   That property is responsive to [config-cascade](http://oxpedia.org/wiki/index.php?title=ConfigCascade). Hence it can be specified for user, context, context-set or server scope.
   For instance, create file `pns.properties` in Open-Xchange configuration directory (`/opt/open-xchange/etc`) and add line `com.openexchange.pns.transport.websocket.enabled=true=true` to globally enable Push via Web Sockets

## Configuration

### Queueing/buffering & processing

The following settings control buffering and queueing as well as processing of push notification messages that are supposed to be transported via a concrete channel (Web Sockets, APNS, GCM, etc.)

* `com.openexchange.pns.delayDuration`<br>
 The time in milliseconds a notification is queued in buffer to possible aggregate with similar notifications that arrive during that time.<br>
Default is 1000 milliseconds.
* `com.openexchange.pns.timerFrequency`<br>
 The frequency/delay in milliseconds when the buffering queue will be checked for due notifications (the ones exceeding delayDuration in queue).<br>
Default is 500 milliseconds.
* `com.openexchange.pns.numProcessorThreads`<br>
 Specifies the number of threads that concurrently handle due notifications that were transferred from buffering queue to processing queue.<br>
 Default is 10.
* `com.openexchange.pns.maxProcessorTasks`<br>
Specifies the buffer size for due notifications that were transferred from buffering queue to processing queue.<br>
Default is 65536.

### Web Sockets transport

The following setting controls whether a push notification message is allowed to be transported to associated user using Web Socket transport

* `com.openexchange.pns.transport.websocket.enabled`<br>
 Specifies whether the Web Socket transport is enabled. That property is responsive to config-cascade and reloadable as well.<br><br>
 Moreover, an even finer-grained decision is possible to be configured as a certain transport is checked for availability providing user, context, client and topic.
Hence, it is possible to append client and topic to the property name according to following pattern<br>
`com.openexchange.pns.transport.websocket.enabled + ("." + {client})? + ("." + {topic})?`
<br><br>
Example
`com.openexchange.pns.transport.websocket.enabled.open-xchange-appsuite.ox:mail:new=true`
`com.openexchange.pns.transport.websocket.enabled.open-xchange-appsuite.ox:calendar:new=false`<br>
That allows the client "open-xchange-appsuite" (App Suite UI) to receive "new mail" notifications
via Web Socket, but not for "new appointment".

### APNS transport

The following setting controls whether a push notification message is allowed to be transported to associated user using APNS transport

* `com.openexchange.pns.transport.apn.ios.enabled`<br>
 Specifies whether the APNS transport is enabled. That property is responsive to config-cascade and reloadable as well.<br><br>
 Moreover, an even finer-grained decision is possible to be configured as a certain transport is checked for availability providing user, context, client and topic.
Hence, it is possible to append client and topic to the property name according to following pattern<br>
`com.openexchange.pns.transport.apn.ios.enabled + ("." + {client})? + ("." + {topic})?`
<br><br>
Example
`com.openexchange.pns.transport.apn.ios.enabled.open-xchange-appsuite.ox:mail:new=true`
`com.openexchange.pns.transport.apn.ios.enabled.open-xchange-appsuite.ox:calendar:new=false`<br>
That allows the client "open-xchange-appsuite" (App Suite UI) to receive "new mail" notifications
via APNS, but not for "new appointment".
* `com.openexchange.pns.transport.apn.ios.feedbackQueryInterval`<br>
 Specifies the frequency in milliseconds when to query the Apple feedback service to check for expired
and/or invalid tokens.<br>
Default is 3600000 (1 hour).

### GCM transport

The following setting controls whether a push notification message is allowed to be transported to associated user using GCM transport

* `com.openexchange.pns.transport.gcm.enabled`<br>
 Specifies whether the GCM transport is enabled. That property is responsive to config-cascade and reloadable as well.<br><br>
 Moreover, an even finer-grained decision is possible to be configured as a certain transport is checked for availability providing user, context, client and topic.
Hence, it is possible to append client and topic to the property name according to following pattern<br>
`com.openexchange.pns.transport.gcm.enabled + ("." + {client})? + ("." + {topic})?`
<br><br>
Example
`com.openexchange.pns.transport.gcm.enabled.open-xchange-appsuite.ox:mail:new=true`
`com.openexchange.pns.transport.gcm.enabled.open-xchange-appsuite.ox:calendar:new=false`<br>
That allows the client "open-xchange-appsuite" (App Suite UI) to receive "new mail" notifications
via GCM, but not for "new appointment".

### WNS transport

The following setting controls whether a push notification message is allowed to be transported to associated user using WNS transport

* `com.openexchange.pns.transport.wns.enabled`<br>
 Specifies whether the WNS transport is enabled. That property is responsive to config-cascade and reloadable as well.<br><br>
 Moreover, an even finer-grained decision is possible to be configured as a certain transport is checked for availability providing user, context, client and topic.
Hence, it is possible to append client and topic to the property name according to following pattern<br>
`com.openexchange.pns.transport.wns.enabled + ("." + {client})? + ("." + {topic})?`
<br><br>
Example
`com.openexchange.pns.transport.wns.enabled.open-xchange-appsuite.ox:mail:new=true`
`com.openexchange.pns.transport.wns.enabled.open-xchange-appsuite.ox:calendar:new=false`<br>
That allows the client "open-xchange-appsuite" (App Suite UI) to receive "new mail" notifications
via WNS, but not for "new appointment".
