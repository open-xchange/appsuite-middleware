---
title: Ports Overview
icon: fa-random
tags: Administration, Configuration
---

This document describes the ports the AppSuite middleware listens on and their related configuration properties.

| Default port | Purpose | Configuration |
|:-------------|:--------|:--------------|
| 143 | Port on which the IMAP server is listening. | [IMAP_PORT](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#IMAP_PORT) |
| 1099 | The RMI port. | [com.openexchange.rmi.port](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.rmi.port) |
| 5701 | The port Hazelcast will listen for incoming connections. | [com.openexchange.hazelcast.network.port](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.hazelcast.network.port), [com.openexchange.hazelcast.network.portOffset](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.hazelcast.network.portOffset)  |
| 8009 | The default port for the connector's HTTP network listener. | [com.openexchange.connector.networkListenerPort](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.connector.networkListenerPort) |
| 8010 | The default port for the connector's HTTPs network listener. | [com.openexchange.connector.networkSslListenerPort](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.connector.networkSslListenerPort) |
| 8016 | The default port for the liveness probe end-point. | [com.openexchange.connector.livenessPort](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.connector.livenessPort) |
| 9982 | Specifies the multicast port for push. | [com.openexchange.push.udp.multicastPort](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.push.udp.multicastPort) |
| 9999 | The port for the RMI Registry. | [JMXPort](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#JMXPort) |
| 44335 | The port where the clients send the push registration request to. | [com.openexchange.push.udp.registerPort](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.push.udp.registerPort) |
| Random | The JMX RMI Connector Server port. Typically chosen randomly by the JVM. | [JMXServerPort](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#JMXServerPort) |
