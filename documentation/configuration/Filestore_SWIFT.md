---
title: Filestore SWIFT
---

Configuration file for SWIFT file storages
This file provides the configuration of all file storages based on the
OpenStack Swift API. Each connected storage is identified by a "filestore ID",
which refers to the authority part of the URI configured  in the "uri" column
in the "filestore" table of the config database 
(previously registered using "./registerfilestore -t [filestoreID]").
For each configured filestore, an own set of the properties may be defined, 
replacing [filestoreID] with the actual identifier.


| __Key__ | com.openexchange.filestore.swift.[filestoreID].protocol |
|:----------------|:--------|
| __Description__ | Specifies the protocol to be used for network communication (http or https)<br>Required.<br> |
| __Default__ | https |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].hosts |
|:----------------|:--------|
| __Description__ | Specifies the API end-point pairs to be used. At least one host must be provided.<br>Multiple hosts can be specified as comma-separated list; e.g. "my1.clouddrive.invalid, my2.clouddrive.invalid"<br>Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].path |
|:----------------|:--------|
| __Description__ | The path consisting of API version and tenant identifier; e.g. "/v1/MyFS_aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"<br>Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].userName |
|:----------------|:--------|
| __Description__ | Specifies the user name to use for authentication.<br>Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].tenantName |
|:----------------|:--------|
| __Description__ | Specifies the tenant name to use for authentication.<br>Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].authType |
|:----------------|:--------|
| __Description__ | Specifies the authentication type to use for authentication against Identity API v2.0;<br>see (http://developer.openstack.org/api-ref-identity-v2.html)<br>Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].authValue |
|:----------------|:--------|
| __Description__ | Specifies the authentication value to use for authentication against Identity API v2.0;<br>see (http://developer.openstack.org/api-ref-identity-v2.html)<br>Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].identityUrl |
|:----------------|:--------|
| __Description__ | The URL for the Identity API v2.0 end-point; e.g. "https://identity.api.mycloud.invalid/v2.0/tokens"<br>Not needed in case "authType" is set to "raxkey" (implicitly set to "https://identity.api.rackspacecloud.com/v2.0/tokens").<br>Required (for "authType" different from "raxkey")<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].maxConnections |
|:----------------|:--------|
| __Description__ | The max. number of concurrent HTTP connections that may be established with the swift<br>endpoints. If you have specified more than one hosts, this setting should be configured<br>so that maxConnectionsPerHost < maxConnections <= n \* maxConnectionsPerHost.<br> |
| __Default__ | 100 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].maxConnectionsPerHost |
|:----------------|:--------|
| __Description__ | The max. number of concurrent HTTP connections that may be established with a certain<br>swift endpoint.<br> |
| __Default__ | 100 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].connectionTimeout |
|:----------------|:--------|
| __Description__ | The connection timeout in milliseconds. If establishing a new HTTP connection to a certain<br>host, it is blacklisted until it is considered available again. A periodic heartbeat task<br>that tries to read the namespace configuration (<protocol>://<host>/<path>/.conf) decides<br>whether an endpoint is considered available again.<br> |
| __Default__ | 5000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].socketReadTimeout |
|:----------------|:--------|
| __Description__ | The socket read timeout in milliseconds. If waiting for the next expected TCP packet exceeds<br>this value, the host is blacklisted until it is considered available again. A periodic heartbeat<br>task that tries to read the namespace configuration (<protocol>://<host>/<path>/.conf) decides<br>whether an endpoint is considered available again.<br> |
| __Default__ | 15000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].heartbeatInterval |
|:----------------|:--------|
| __Description__ | Hosts can get blacklisted if the client considers them to be unavailable. All hosts on the<br>blacklist are checked periodically if they are available again and are then removed from the<br>blacklist if so. A host is considered available again if the namespace configuration file<br>(<protocol>://<host>/<path>/.conf) can be requested without any error. This setting specifies<br>the interval in milliseconds between two heartbeat runs. The above specified timeouts must be<br>taken into account for specifying a decent value, as every heartbeat run might block until a<br>timeout happens for every still unavailable host.<br> |
| __Default__ | 60000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a> |
| __File__ | filestore-swift.properties |

---
