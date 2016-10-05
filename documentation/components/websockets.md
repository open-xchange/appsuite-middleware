---
title: Web Sockets
---

# Web Sockets documentation

With Open-Xchange Middleware version 7.8.3 Web Socket connections are supported. A Web Socket connection is only allowed to be established for a fully authenticated session. Hence, a HTTP Upgrade request is required to pass all the validating and verifying steps as for regular HTTP requests in order to establish a Web Socket connection.

Moreover, a Web Socket connection is cluster-wide registered. Thus a message created on cluster member A reaches possible open Web Sockets on cluster member B.

## Prerequisites

In order to use Web Socket transport the load-balancing and/or proxy'ing Web Server needs to be prepared to also
accept and manage Web Socket connections. For Apache the [`mod_proxy_wstunnel`](https://httpd.apache.org/docs/2.4/mod/mod_proxy_wstunnel.html)
module is the appropriate choice in addition to the [`mod_proxy`](https://httpd.apache.org/docs/2.4/mod/mod_proxy.html) module.

Following the [exemplary best-practice configuration for Apache](http://oxpedia.org/wiki/index.php?title=AppSuite:Grizzly#Apache_configuration)
the `mod_proxy_wstunnel` module needs at first to be enabled:

```
  $ a2enmod mod_proxy_wstunnel
```

Assuming there is already a proxy configuration for Open-Xchange Groupware nodes; e.g.

```
    # Define default Proxy container
    <Proxy balancer://oxcluster>
        Order Allow,Deny
        Allow from all
        BalancerMember http://ox1.open-xchange.com:8009 timeout=100 smax=0 ttl=60 retry=60 loadfactor=50 keepalive=On route=OX1
        BalancerMember http://ox2.open-xchange.com:8009 timeout=100 smax=0 ttl=60 retry=60 loadfactor=50 keepalive=On route=OX2
        ProxySet stickysession=JSESSIONID|jsessionid scolonpathdelim=On
    </Proxy>
```

Simply add a section for those nodes that are supposed to be accessible by Web Socket connections:

```
    <Proxy balancer://oxcluster_ws>
        Order Allow,Deny
        Allow from all
        BalancerMember ws://ox1.open-xchange.com:8009 timeout=100 smax=0 ttl=60 retry=60 loadfactor=50 keepalive=On route=OX1
        BalancerMember ws://ox2.open-xchange.com:8009 timeout=100 smax=0 ttl=60 retry=60 loadfactor=50 keepalive=On route=OX2
        ProxySet stickysession=JSESSIONID|jsessionid scolonpathdelim=On
    </Proxy>
```

As last step, there needs to be a `ProxyPass` directive for the `socket.io` path dedicated to Web Sockets:

```
    ProxyPass /ajax balancer://oxcluster/ajax
    ProxyPass /appsuite/api balancer://oxcluster/ajax
     ...

    ProxyPass /socket.io balancer://oxcluster_ws/socket.io
```

As a Web Socket is made cluster-wide accessible, Hazelcast is used to manage the orchestration and management of Web Sockets in the cluster. If Hazelcast is not installed, inter node communication is not supported.

## Installation

1. Install the "open-xchange-websockets-grizzly" package
2. Enable the `com.openexchange.websockets.enabled` property.
   That property is responsive to [config-cascade](http://oxpedia.org/wiki/index.php?title=ConfigCascade). Hence it can be specified for user, context, context-set or server scope.
   For instance, create file `websockets.properties` in Open-Xchange configuration directory (`/opt/open-xchange/etc`) and add line `com.openexchange.websockets.enabled=true` to globally enabled Web Sockets

## Configuration

### Enable/disable

As already outlined above, the config-cascade-aware property `com.openexchange.websockets.enabled`controls whether a Web Socket is allowed to be created for a certain user.

### Remote communication

The following settings control buffering and queueing of Web Socket messages that are supposed to be transferred to a remote cluster member

* `com.openexchange.websockets.grizzly.remote.delayDuration`
  The time in milliseconds a message (that is supposed to be transferred to a remote cluster member)
  is queued in buffer to await equal message that arrive during that time.
  Default 1000ms
* `com.openexchange.websockets.grizzly.remote.maxDelayDuration`
  The time in milliseconds a "remote" message is at max. queued in buffer to await
  equal message that arrive during that time. So, even is there was an equal
  message recently, message is flushed from queue to avoid holding back a
  message forever in case there are frequent equal messages.
  Default 3000ms
* `com.openexchange.websockets.grizzly.remote.timerFrequency`
  The frequency/delay in milliseconds when the buffering queue will be checked for due
  "remote" messages (the ones exceeding delayDuration in queue).
  Default 500ms

These settings are not config-cascade-aware, but reloadable.

