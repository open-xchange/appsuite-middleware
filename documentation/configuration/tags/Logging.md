---
title: Logging
---

This page shows all properties with the tag: Logging

| __Key__ | testThreads |
|:----------------|:--------|
| __Description__ | If testThreads is set to true, more information is logged to the Open-Xchange<br>log files about database connections.  If this option is enabled the<br>performance may degrade dramatically. The JVM has to generate then a lot of<br>method call stack dumps.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Logging.html">Logging</a> |
| __File__ | configdb.properties |

---
| __Key__ | com.openexchange.database.checkWriteCons |
|:----------------|:--------|
| __Description__ | If com.openexchange.database.checkWriteCons is set to true, a warning will be logged every time when a writable connection is used to<br>perform only SELECT queries. Default is 'false' to avoid flooding of log files.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Logging.html">Logging</a> |
| __File__ | configdb.properties |

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
