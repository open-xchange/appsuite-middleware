---
title: Hazelcast
---

This page shows all properties with the tag: Hazelcast

| __Key__ | com.openexchange.hazelcast.enabled |
|:----------------|:--------|
| __Description__ | Enables or disables Hazelcast. Setting this property to "false" will result<br>in no Hazelcast instance being created for this node, and all other<br>dependent features will be disabled.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.shutdownOnOutOfMemory |
|:----------------|:--------|
| __Description__ | Specifies if Hazelccast is supposed to be shut-down in case an Out-Of-Memory error occurred.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.group.name |
|:----------------|:--------|
| __Description__ | Configures the name of the cluster. Only nodes using the same group name<br>will join each other and form the cluster. Required if<br>"com.openexchange.hazelcast.network.join" is not "empty".<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.hazelcast.network.join |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.group.password |
|:----------------|:--------|
| __Description__ | The password used when joining the cluster. <br>Please change this value, and ensure it's equal on all nodes in the cluster!!!<br> |
| __Default__ | wtV6$VQk8#+3ds!a |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.network.join |
|:----------------|:--------|
| __Description__ | Specifies which mechanism is used to discover other backend nodes in the<br>cluster. Possible values are "empty" (no discovery for single-node setups),<br>"static" (fixed set of cluster member nodes) or "multicast" (automatic<br>discovery of other nodes via multicast). Depending on the specified value, <br>further configuration might be needed.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.network.join.static.nodes |
|:----------------|:--------|
| __Description__ | Configures a comma-separated list of IP addresses / hostnames of possible<br>nodes in the cluster, e.g. "10.20.30.12, 10.20.30.13:5701, 192.178.168.110".<br>Only used if "com.openexchange.hazelcast.network.join" is set to "static".<br>It doesn't hurt if the address of the local host appears in the list, so<br>that it's still possible to use the same list throughout all nodes in the<br>cluster.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.hazelcast.network.join |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.network.join.multicast.group |
|:----------------|:--------|
| __Description__ | Configures the multicast address used to discover other nodes in the cluster<br>dynamically. Only used if "com.openexchange.hazelcast.network.join" is set<br>to "multicast". If the nodes reside in different subnets, please ensure that<br>multicast is enabled between the subnets.<br> |
| __Default__ | 224.2.2.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.hazelcast.network.join |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.network.join.multicast.port |
|:----------------|:--------|
| __Description__ | Configures the multicast port used to discover other nodes in the cluster<br>dynamically. Only used if "com.openexchange.hazelcast.network.join" is set<br>to "multicast".<br> |
| __Default__ | 54327 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.hazelcast.network.join |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.merge.firstRunDelay |
|:----------------|:--------|
| __Description__ | Configures the time until the first check if the cluster needs to merge is<br>scheduled. This takes a timespan parameter with "ms" denoting milliseconds,<br>"s" denoting seconds, "m" denoting minutes. The value is passed to the<br>hazelcast property "hazelcast.merge.first.run.delay.seconds". Defaults to<br>"120s".<br> |
| __Default__ | 120s |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.merge.runDelay |
|:----------------|:--------|
| __Description__ | Configures the time between each check if the cluster needs to merge. This<br>takes a timespan parameter with "ms" denoting milliseconds, "s" denoting<br>seconds, "m" denoting minutes. The value is passed to the hazelcast property<br>"hazelcast.merge.next.run.delay.seconds".<br> |
| __Default__ | 120s |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.network.interfaces |
|:----------------|:--------|
| __Description__ | Comma-separated list of interface addresses hazelcast should use. Wildcards<br>(\*) and ranges (-) can be used. Leave blank to listen on all interfaces<br>Especially in server environments with multiple network interfaces, it's<br>recommended to specify the IP-address of the network interface to bind to<br>explicitly. Defaults to "127.0.0.1" (local loopback only), needs to be<br>adjusted when building a cluster of multiple backend nodes.<br> |
| __Default__ | 127.0.0.1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.network.port |
|:----------------|:--------|
| __Description__ | The port Hazelcast will listen for incoming connections.<br> |
| __Default__ | 5701 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.network.portAutoIncrement |
|:----------------|:--------|
| __Description__ | Configures if automatically the next port should be tried if the incoming<br>port is already in use.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.network.outboundPortDefinitions |
|:----------------|:--------|
| __Description__ | By default, Hazelcast lets the system to pick up an ephemeral port during<br>socket bind operation. But security policies/firewalls may require to<br>restrict outbound ports to be used by Hazelcast enabled applications. To<br>fulfill this requirement, you can configure Hazelcast to use only defined<br>outbound ports. You can use port ranges and/or comma separated ports, e.g.<br>"35000-35100" or "36001, 36002, 36003".<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.network.enableIPv6Support |
|:----------------|:--------|
| __Description__ | Enables or disables support for IPv6.  IPv6 support is switched off by<br>default, since some platforms have issues in use of IPv6 stack, and some<br>other platforms such as Amazon AWS have no support at all.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.socket.bindAny |
|:----------------|:--------|
| __Description__ | Configures whether to bind the server- and client-sockets to any local<br>interface or not. Defaults to "false", which restricts the bind operation to<br>the picked interface address based on the value of<br>"com.openexchange.hazelcast.network.interfaces".<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.network.symmetricEncryption |
|:----------------|:--------|
| __Description__ | Enables or disables symmetric encryption. When enabled, the entire<br>communication between the hazelcast members is encrypted at socket level.<br>Ensure that all symmetric encryption settings are equal on all<br>participating nodes in the cluster. More advanced options (including<br>asymmetric encryption and SSL) may still be configured via the<br>"hazelcast.xml" file, see instructions on top of this file.<br><br> /!\ ---==== Additional note ====--- /!\<br>If symmetric encryption is enabled, it might have impact on Hazelcast<br>cluster stability. Hazelcast nodes start loosing cluster connectivity under<br>high load scenarios<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encryption.html">Encryption</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.network.symmetricEncryption.algorithm |
|:----------------|:--------|
| __Description__ | Configures the name of the symmetric encryption algorithm to use, such as<br>"DES/ECB/PKCS5Padding", "PBEWithMD5AndDES", "Blowfish" or "DESede". The<br>available cipher algorithms may vary based on the underlying JCE.<br> |
| __Default__ | PBEWithMD5AndDES |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encryption.html">Encryption</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.network.symmetricEncryption.salt |
|:----------------|:--------|
| __Description__ | Specifies the salt value to use when generating the secret key for symmetric<br>encryption.<br> |
| __Default__ | 2mw67LqNDEb3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encryption.html">Encryption</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.network.symmetricEncryption.password |
|:----------------|:--------|
| __Description__ | Specifies the pass phrase to use when generating the secret key for<br>symmetric encryption.<br> |
| __Default__ | 2mw67LqNDEb3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encryption.html">Encryption</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.network.symmetricEncryption.iterationCount |
|:----------------|:--------|
| __Description__ | Configures the iteration count to use when generating the secret key for<br>symmetric encryption. <br> |
| __Default__ | 19 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encryption.html">Encryption</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.healthMonitorLevel |
|:----------------|:--------|
| __Description__ | Controls the log level for regular statistics of the health monitor. <br>Possible values include "off" (disables the health monitor), "silent" <br>(prints out statistics if certain thresholds are exceeded) and "noisy" <br>(always prints out statistics). Defaults to "silent".<br>Note: Please also check the configured log level for <br>"com.hazelcast.internal.monitors.HealthMonitor".<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Logging.html">Logging</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.maxOperationTimeout |
|:----------------|:--------|
| __Description__ | Specifies the implicit maximum operation timeout in milliseconds for<br>operations on distributed data structures, if no explicit timeout is<br>specified for an operation.<br> |
| __Default__ | 30000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.jmx |
|:----------------|:--------|
| __Description__ | Enables or disables JMX monitoring for hazelcast components such as<br>statistics about distributed data structures.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Monitoring.html">Monitoring</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.jmxDetailed |
|:----------------|:--------|
| __Description__ | Specifies whether detailed JMX monitoring is enabled or not, i.e. detailed<br>information about entries in distributed data structures. Only taken into<br>account if "com.openexchange.hazelcast.jmx" is "true".<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.hazelcast.jmx |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Monitoring.html">Monitoring</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.jmxDetailed |
|:----------------|:--------|
| __Description__ | Specifies whether detailed JMX monitoring is enabled or not, i.e. detailed<br>information about entries in distributed data structures. Only taken into<br>account if "com.openexchange.hazelcast.jmx" is "true".<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.hazelcast.jmx |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Monitoring.html">Monitoring</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.memcache.enabled |
|:----------------|:--------|
| __Description__ | Enables or disables Hazelcast's internal Memcache client request listener<br>service. Defaults to "false", as it's not needed by the backend.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.rest.enabled |
|:----------------|:--------|
| __Description__ | Enables or disables Hazelcast's internal REST client request listener<br>service. Defaults to "false", as it's not needed by the backend.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/REST.html">REST</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.liteMember |
|:----------------|:--------|
| __Description__ | Allows to configure a node as "lite" member. <br>Lite members are the Hazelcast cluster members that do not store data. These members are used mainly to execute tasks and register listeners, and they do not have partitions.<br> |
| __Default__ | false |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.sessionstorage.hazelcast.enabled |
|:----------------|:--------|
| __Description__ | Enabled/disable Hazelcast-based session storage.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Session.html">Session</a> |
| __File__ | sessionstorage-hazelcast.properties |

---
