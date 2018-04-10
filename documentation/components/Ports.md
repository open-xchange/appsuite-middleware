---
title: Ports overview
---

This document describes the ports the appsuite middleware listens on and their related config properties.

| Default port | Purpose | Configuration |
|:-------------|:--------|:--------------|
| 143 | Port on which the IMAP server is listening. | [IMAP_PORT]((http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#IMAP_PORT) |
| 1099 | The rmi port. | [com.openexchange.rmi.port](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.rmi.port) |
| 5701 | The port Hazelcast will listen for incoming connections. | [com.openexchange.hazelcast.network.port](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.hazelcast.network.port) |
| 8009 | The default port for the connector's http network listener. | [com.openexchange.connector.networkListenerPort](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.connector.networkListenerPort) |
| 8010 | The default port for the connector's https network listener. | [com.openexchange.connector.networkSslListenerPort](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.connector.networkSslListenerPort) |
| 9982 | Specifies the multicast port for push. | [com.openexchange.push.udp.multicastPort](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.push.udp.multicastPort) |
| 9999 | The port for the RMI Registry. | [JMXPort](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#JMXPort) |
| 44335 | The port where the clients send the push registration request to. | [com.openexchange.push.udp.registerPort](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.push.udp.registerPort) |
| Random | The JMX RMI Connector Server port. Typically chosen randomly by the JVM. | [JMXServerPort](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#JMXServerPort) |
