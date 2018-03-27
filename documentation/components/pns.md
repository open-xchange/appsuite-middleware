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

* [com.openexchange.pns.delayDuration](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.pns.delayDuration)  
 The time in milliseconds a notification is queued in buffer to possible aggregate with similar notifications that arrive during that time.  
 Default is 1000 milliseconds.
* [com.openexchange.pns.timerFrequency](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.pns.timerFrequency)  
 The frequency/delay in milliseconds when the buffering queue will be checked for due notifications (the ones exceeding delayDuration in queue).  
 Default is 500 milliseconds.
* [com.openexchange.pns.numProcessorThreads](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.pns.numProcessorThreads)  
 Specifies the number of threads that concurrently handle due notifications that were transferred from buffering queue to processing queue.  
 Default is 10.
* [com.openexchange.pns.maxProcessorTasks](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.pns.maxProcessorTasks)  
 Specifies the buffer size for due notifications that were transferred from buffering queue to processing queue.  
 Default is 65536.

### Web Sockets transport

The following setting controls whether a push notification message is allowed to be transported to associated user using Web Socket transport

* [com.openexchange.pns.transport.websocket.enabled](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.pns.transport.websocket.enabled)  
 Specifies whether the Web Socket transport is enabled. That property is responsive to config-cascade and reloadable as well.

Moreover, an even finer-grained decision is possible to be configured as a certain transport is checked for availability providing user, context, client and topic.
Hence, it is possible to append client and topic to the property name according to following pattern: 
`com.openexchange.pns.transport.websocket.enabled + ("." + {client})? + ("." + {topic})?`

Example:
```
com.openexchange.pns.transport.websocket.enabled.open-xchange-appsuite.ox:mail:new=true
com.openexchange.pns.transport.websocket.enabled.open-xchange-appsuite.ox:calendar:new=false
```
That allows the client "open-xchange-appsuite" (App Suite UI) to receive "new mail" notifications
via Web Socket, but not for "new appointment".

### APNS transport

The following setting controls whether a push notification message is allowed to be transported to associated user using APNS transport

* [com.openexchange.pns.transport.apn.ios.enabled](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.pns.transport.apn.ios.enabled)  
 Specifies whether the APNS transport is enabled. That property is responsive to config-cascade and reloadable as well.

Moreover, an even finer-grained decision is possible to be configured as a certain transport is checked for availability providing user, context, client and topic.
Hence, it is possible to append client and topic to the property name according to following pattern:  
`com.openexchange.pns.transport.apn.ios.enabled + ("." + {client})? + ("." + {topic})?`

Example:
```
com.openexchange.pns.transport.apn.ios.enabled.open-xchange-appsuite.ox:mail:new=true
com.openexchange.pns.transport.apn.ios.enabled.open-xchange-appsuite.ox:calendar:new=false
```
That allows the client "open-xchange-appsuite" (App Suite UI) to receive "new mail" notifications
via APNS, but not for "new appointment".
* [com.openexchange.pns.transport.apn.ios.feedbackQueryInterval](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.pns.transport.apn.ios.feedbackQueryInterval)  
 Specifies the frequency in milliseconds when to query the Apple feedback service to check for expired and/or invalid tokens.  
 Default is 3600000 (1 hour).

Furthermore the actual APNS options need to be configured on a per client basis. APNS options are specified in the ``/opt/open-xchange/etc/pns-apns-options.yml`` file; e.g.

```
# Only an example
myiosclient:
    # Disabled...
    enabled: false
    keystore: /opt/open-xchange/etc/mykey-apns.p12
    password: A3JWKAKR8XB
    production: true
```

In this example, ``myiosclient`` is the identifier of the client, to which the push notifications are supposed to be routed. Below a certain client identifier, the options specify:

* `enabled`  
  Boolean. If set to "false" the client configuration will not be available. Default is "true".
* `keystore`  
  String. Specifies the path to the local keystore file (PKCS #12) containing the APNS certificate and keys for the client-associated iOS application
* `password`  
  String. Specifies the password to use when creating the referenced keystore containing the certificate of the iOS application.
* `production`  
  Boolean. Indicates which APNS service is used when sending push notifications to iOS devices. A value of "true" will use the production service, a value of "false" references to the sandbox service.  Default is "true".

### GCM transport

The following setting controls whether a push notification message is allowed to be transported to associated user using GCM transport

* [com.openexchange.pns.transport.gcm.enabled](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.pns.transport.gcm.enabled)  
 Specifies whether the GCM transport is enabled. That property is responsive to config-cascade and reloadable as well.

Moreover, an even finer-grained decision is possible to be configured as a certain transport is checked for availability providing user, context, client and topic.
Hence, it is possible to append client and topic to the property name according to following pattern:  
`com.openexchange.pns.transport.gcm.enabled + ("." + {client})? + ("." + {topic})?`

Example:
```
com.openexchange.pns.transport.gcm.enabled.open-xchange-appsuite.ox:mail:new=true
com.openexchange.pns.transport.gcm.enabled.open-xchange-appsuite.ox:calendar:new=false
```
That allows the client "open-xchange-appsuite" (App Suite UI) to receive "new mail" notifications
via GCM, but not for "new appointment".

Furthermore the actual GCM options need to be configured on a per client basis. GCM options are specified in the ``/opt/open-xchange/etc/pns-gcm-options.yml`` file; e.g.

```
# Only an example
mygoogleclient:
    # Disabled...
    enabled: false
    key: AIzaSy2535345TbVL2r4yaZ4ZVQvJdcE1vth24546
```

In this example, ``mygoogleclient`` is the identifier of the client, to which the push notifications are supposed to be routed. Below a certain client identifier, the options specify:

* `enabled`  
  Boolean. If set to "false" the client configuration will not be available. Default is "true".
* `key`  
  String. Specifies the API key of the server application.

### WNS transport

The following setting controls whether a push notification message is allowed to be transported to associated user using WNS transport

* [com.openexchange.pns.transport.wns.enabled](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.pns.transport.wns.enabled)  
 Specifies whether the WNS transport is enabled. That property is responsive to config-cascade and reloadable as well.<br><br>

Moreover, an even finer-grained decision is possible to be configured as a certain transport is checked for availability providing user, context, client and topic.
Hence, it is possible to append client and topic to the property name according to following pattern:  
`com.openexchange.pns.transport.wns.enabled + ("." + {client})? + ("." + {topic})?`

Example:
```
com.openexchange.pns.transport.wns.enabled.open-xchange-appsuite.ox:mail:new=true
com.openexchange.pns.transport.wns.enabled.open-xchange-appsuite.ox:calendar:new=false
```
That allows the client "open-xchange-appsuite" (App Suite UI) to receive "new mail" notifications
via WNS, but not for "new appointment".

Furthermore the actual WNS options need to be configured on a per client basis. WNS options are specified in the ``/opt/open-xchange/etc/pns-wns-options.yml`` file; e.g.

```
# Only an example
mywindowsclient:
    # Disabled...
    enabled: false
    sid: AIzaSy2535345TbVL2r4yaZ4ZVQvJdcE1vth24546
    secret: 14e435y2535345TbVL2r4yaZ4ZVQvJdcE1vth24546
```

In this example, ``mywindowsclient`` is the identifier of the client, to which the push notifications are supposed to be routed. Below a certain client identifier, the options specify:

* `enabled`  
  Boolean. If set to "false" the client configuration will not be available. Default is "true".
* `sid`  
  String. Specifies the SID (Package security identifier).
* `secret`  
  String. Specifies the client secret.