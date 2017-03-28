---
title: Config DB
---

This page describes the properties needed to configure the JDBC connections to the configdb.
The configdb holds data about where to find context specific data.
The driver properties should be kept at their defaults.


| __Key__ | readDriverClass |
|:----------------|:--------|
| __Description__ | The read driver class.<br> |
| __Default__ | com.mysql.jdbc.Driver |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | configdb.properties |

---
| __Key__ | readUrl |
|:----------------|:--------|
| __Description__ | The readURL holds the database host and the used schema name.<br>The read connection must point to the database slave.<br> |
| __Default__ | jdbc:mysql://localhost/configdb |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | configdb.properties |

---
| __Key__ | readProperty.1 |
|:----------------|:--------|
| __Description__ | The db user name.<br> |
| __Default__ | user=openexchange |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | configdb.properties |

---
| __Key__ | readProperty.2 |
|:----------------|:--------|
| __Description__ | The database password.<br> |
| __Default__ | password=secret |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | configdb.properties |

---
| __Key__ | readProperty.3 |
|:----------------|:--------|
| __Description__ | A property of the db read connection. Should be kept at its default.<br> |
| __Default__ | useUnicode=true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | configdb.properties |

---
| __Key__ | readProperty.4 |
|:----------------|:--------|
| __Description__ | A property of the db read connection. Should be kept at its default.<br> |
| __Default__ | characterEncoding=UTF-8 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | configdb.properties |

---
| __Key__ | readProperty.5 |
|:----------------|:--------|
| __Description__ | A property of the db read connection. Should be kept at its default.<br> |
| __Default__ | autoReconnect=false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | configdb.properties |

---
| __Key__ | readProperty.6 |
|:----------------|:--------|
| __Description__ | A property of the db read connection. Should be kept at its default.<br> |
| __Default__ | useServerPrepStmts=false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | configdb.properties |

---
| __Key__ | readProperty.7 |
|:----------------|:--------|
| __Description__ | A property of the db read connection. Should be kept at its default.<br> |
| __Default__ | useTimezone=true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | configdb.properties |

---
| __Key__ | readProperty.8 |
|:----------------|:--------|
| __Description__ | A property of the db read connection. Should be kept at its default.<br> |
| __Default__ | serverTimezone=UTC |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | configdb.properties |

---
| __Key__ | readProperty.9 |
|:----------------|:--------|
| __Description__ | A property of the db read connection. Should be kept at its default.<br> |
| __Default__ | connectTimeout=15000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | configdb.properties |

---
| __Key__ | readProperty.10 |
|:----------------|:--------|
| __Description__ | A property of the db read connection. Should be kept at its default.<br> |
| __Default__ | socketTimeout=15000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | configdb.properties |

---
| __Key__ | writeDriverClass |
|:----------------|:--------|
| __Description__ | The write driver class.<br> |
| __Default__ | com.mysql.jdbc.Driver |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | configdb.properties |

---
| __Key__ | readUrl |
|:----------------|:--------|
| __Description__ | The writeURL holds the database host and the used schema name.<br>The write connection must point to the database master.<br> |
| __Default__ | jdbc:mysql://localhost/configdb |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | configdb.properties |

---
| __Key__ | writeProperty.1 |
|:----------------|:--------|
| __Description__ | The db user name.<br> |
| __Default__ | user=openexchange |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | configdb.properties |

---
| __Key__ | writeProperty.2 |
|:----------------|:--------|
| __Description__ | The database password.<br> |
| __Default__ | password=secret |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | configdb.properties |

---
| __Key__ | writeProperty.3 |
|:----------------|:--------|
| __Description__ | A property of the db write connection. Should be kept at its default.<br> |
| __Default__ | useUnicode=true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | configdb.properties |

---
| __Key__ | writeProperty.4 |
|:----------------|:--------|
| __Description__ | A property of the db write connection. Should be kept at its default.<br> |
| __Default__ | characterEncoding=UTF-8 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | configdb.properties |

---
| __Key__ | writeProperty.5 |
|:----------------|:--------|
| __Description__ | A property of the db write connection. Should be kept at its default.<br> |
| __Default__ | autoReconnect=false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | configdb.properties |

---
| __Key__ | writeProperty.6 |
|:----------------|:--------|
| __Description__ | A property of the db write connection. Should be kept at its default.<br> |
| __Default__ | useServerPrepStmts=false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | configdb.properties |

---
| __Key__ | writeProperty.7 |
|:----------------|:--------|
| __Description__ | A property of the db write connection. Should be kept at its default.<br> |
| __Default__ | useTimezone=true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | configdb.properties |

---
| __Key__ | writeProperty.8 |
|:----------------|:--------|
| __Description__ | A property of the db write connection. Should be kept at its default.<br> |
| __Default__ | serverTimezone=UTC |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | configdb.properties |

---
| __Key__ | writeProperty.9 |
|:----------------|:--------|
| __Description__ | A property of the db write connection. Should be kept at its default.<br> |
| __Default__ | connectTimeout=15000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | configdb.properties |

---
| __Key__ | writeProperty.10 |
|:----------------|:--------|
| __Description__ | A property of the db write connection. Should be kept at its default.<br> |
| __Default__ | socketTimeout=15000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | configdb.properties |

---
| __Key__ | cleanerInterval |
|:----------------|:--------|
| __Description__ | Timeinterval of cleaner thread in milliseconds. <br>This thread removes idle timed out database connections and <br>removes not used database connection pools after each cleanerInterval.<br> |
| __Default__ | 10000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | configdb.properties |

---
| __Key__ | maxIdle |
|:----------------|:--------|
| __Description__ | Number of maximum idle connections. More connections aren't pooled and closed.<br>-1 stands for unlimited.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | configdb.properties |

---
| __Key__ | maxIdleTime |
|:----------------|:--------|
| __Description__ | Maximum time in milliseconds a connection can be idle. If this time is <br>exceeded, the connection gets closed.<br> |
| __Default__ | 60000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | configdb.properties |

---
| __Key__ | maxActive |
|:----------------|:--------|
| __Description__ | If exhaustedAction is set to BLOCK, not more than maxActive connections<br>will be opened to the mysql database.<br>This value is overwritten for OX databases from configdb.<br> |
| __Default__ | 100 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | exhaustedAction |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | configdb.properties |

---
| __Key__ | maxWait |
|:----------------|:--------|
| __Description__ | If exhaustedAction is set to BLOCK, a thread will not wait for more than <br>maxWait milliseconds.<br> |
| __Default__ | 10000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | exhaustedAction |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | configdb.properties |

---
| __Key__ | maxLifeTime |
|:----------------|:--------|
| __Description__ | Maximum time in milliseconds a connection will be used. After this time<br>the connection get closed.<br> |
| __Default__ | 600000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | configdb.properties |

---
| __Key__ | exhaustedAction |
|:----------------|:--------|
| __Description__ | BLOCK: If maxActive number of connections is reached threads have to wait for a connection.<br>FAIL: If maxActive number of connections is reached an exception is thrown.<br>GROW: Open more connections even if maxActive is already reached.<br>This value is overwritten for OX databases from configdb.<br> |
| __Default__ | BLOCK |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | maxWait, maxActive |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | configdb.properties |

---
| __Key__ | testOnActivate |
|:----------------|:--------|
| __Description__ | Validate connections if they are activated. This is not necessary because the<br>activation already includes a check if the connection isn't closed.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | configdb.properties |

---
| __Key__ | testOnDeactivate |
|:----------------|:--------|
| __Description__ | Check if connections can be reused after they are returned to the pool.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | configdb.properties |

---
| __Key__ | testOnIdle |
|:----------------|:--------|
| __Description__ | Not useful for connections.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | configdb.properties |

---
| __Key__ | testThreads |
|:----------------|:--------|
| __Description__ | If testThreads is set to true, more information is logged to the Open-Xchange<br>log files about database connections.  If this option is enabled the<br>performance may degrade dramatically. The JVM has to generate then a lot of<br>method call stack dumps.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Logging.html">Logging</a> |
| __File__ | configdb.properties |

---
| __Key__ | com.openexchange.database.replicationMonitor |
|:----------------|:--------|
| __Description__ | This property allows to disable the replication monitor. This option is only useful if you have a MySQL master and slave setup.<br>If the application code releases a connection to the MySQL master, a writing operation on the database is assumed. To be able to monitor<br>the replication to the slave, a counter is increased after releasing the connection. Reading this counter from the slave indicates, the<br>write operation is not replicated yet and instead of a slave connection a master connection is then used to read data. This prevents data<br>reading inconsistencies. Unfortunately will this produce additional IO load on MySQL master and slave.<br>This mechanism can be disabled with this property. This saves IO load but it may cause data inconsistencies. Especially newly<br>created objects can disappear in the following refresh requests.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
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
