---
title: Filestore sproxyd
---

Configuration file for sproxyd file storages
This file provides the configuration of all file storages based on the
Scality sproxyd API. Each connected storage is identified by a so called 
"filestore ID", which refers to the authority part of the URI configured 
in the "uri" column in the "filestore" table of the config database, 
previously registered using "./registerfilestore -t [filestoreID]".
For each configured filestore, an own set of the properties may be defined, 
replacing [filestoreID] with the actual identifier.


| __Key__ | com.openexchange.filestore.sproxyd.[filestoreID].protocol |
|:----------------|:--------|
| __Description__ | Specifies the protocol to be used for network communication (http or https)<br>Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-sproxyd.properties |

---
| __Key__ | com.openexchange.filestore.sproxyd.[filestoreID].hosts |
|:----------------|:--------|
| __Description__ | Specifies the hosts as <hostname>:<port> pairs to be used for network communication.<br>At least one host must be provided, multiple hosts can be specified as comma-separated<br>list.<br>Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-sproxyd.properties |

---
| __Key__ | com.openexchange.filestore.sproxyd.[filestoreID].path |
|:----------------|:--------|
| __Description__ | The path under which sproxyd is available. The path must lead to the namespace under<br>which OX related files shall be stored. It is expected that the namespace configuration<br>is available under <protocol>://<host>/<path>/.conf.<br>Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-sproxyd.properties |

---
| __Key__ | com.openexchange.filestore.sproxyd.[filestoreID].maxConnections |
|:----------------|:--------|
| __Description__ | The max. number of concurrent HTTP connections that may be established with the sproxyd<br>endpoints. If you have specified more than one hosts, this setting should be configured<br>so that maxConnectionsPerHost < maxConnections <= n \* maxConnectionsPerHost.<br> |
| __Default__ | 100 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-sproxyd.properties |

---
| __Key__ | com.openexchange.filestore.sproxyd.[filestoreID].maxConnectionsPerHost |
|:----------------|:--------|
| __Description__ | The max. number of concurrent HTTP connections that may be established with a certain<br>sproxyd endpoint.<br> |
| __Default__ | 100 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-sproxyd.properties |

---
| __Key__ | com.openexchange.filestore.sproxyd.[filestoreID].connectionTimeout |
|:----------------|:--------|
| __Description__ | The connection timeout in milliseconds. If establishing a new HTTP connection to a certain<br>host, it is blacklisted until it is considered available again. A periodic heartbeat task<br>that tries to read the namespace configuration (<protocol>://<host>/<path>/.conf) decides<br>whether an endpoint is considered available again.<br> |
| __Default__ | 5000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a> |
| __File__ | filestore-sproxyd.properties |

---
| __Key__ | com.openexchange.filestore.sproxyd.[filestoreID].socketReadTimeout |
|:----------------|:--------|
| __Description__ | The socket read timeout in milliseconds. If waiting for the next expected TCP packet exceeds<br>this value, the host is blacklisted until it is considered available again. A periodic heartbeat<br>task that tries to read the namespace configuration (<protocol>://<host>/<path>/.conf) decides<br>whether an endpoint is considered available again.<br> |
| __Default__ | 15000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a> |
| __File__ | filestore-sproxyd.properties |

---
| __Key__ | com.openexchange.filestore.sproxyd.[filestoreID].heartbeatInterval |
|:----------------|:--------|
| __Description__ | Hosts can get blacklisted if the client consieders them to be unavailable. All hosts on the<br>blacklist are checked periodically if they are available again and are then removed from the<br>blacklist if so. A host is considered available again if the namespace configuration file<br>(<protocol>://<host>/<path>/.conf) can be requested without any error. This setting specifies<br>the interval in milliseconds between two heartbeat runs. The above specified timeouts must be<br>taken into account for specifying a decent value, as every heartbeat run might block until a<br>timeout happens for every still unavailable host.<br> |
| __Default__ | 60000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a> |
| __File__ | filestore-sproxyd.properties |

---
