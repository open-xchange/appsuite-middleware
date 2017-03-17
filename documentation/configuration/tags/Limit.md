---
title: Limit
---

This page shows all properties with the tag: Limit

| __Key__ | com.openexchange.mailaccount.failedAuth.limit |
|:----------------|:--------|
| __Description__ | Specifies the max. number of failed authentication attempts until the associated mail account is disabled.<br> |
| __Default__ | 5 |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mailaccount.failedAuth.span |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | mailaccount.properties |

---
| __Key__ | com.openexchange.report.appsuite.maxChunkSize |
|:----------------|:--------|
| __Description__ | Determines how many chunks of data can be kept in the report before saving them in the folder described in the <br>com.openexchange.report.client.fileStorage property<br> |
| __Default__ | 200 |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Report.html">Report</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | reportserialization.properties |

---
| __Key__ | com.openexchange.report.appsuite.maxThreadPoolSize |
|:----------------|:--------|
| __Description__ | Number of threads allowed to work on the report at the same time.<br> |
| __Default__ | 20 |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Report.html">Report</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | reportserialization.properties |

---
| __Key__ | com.openexchange.http.grizzly.maxNumberOfConcurrentRequests |
|:----------------|:--------|
| __Description__ | Specifies the number of concurrent HTTP requests that are allowed being processed.<br>Those requests exceeding that limit will encounter a 503 "The server is temporary overloaded..." status code and accompanying error page<br>A value of less than or equal to 0 (zero) effectively disables that limitation.<br>The chosen value for this property should be aligned to the configured "ulimit" of the backing operating system. E.g. having "ulimit" set<br>to 8,192 (given that JVM is the only main process running for OS user) implies that ~6,000 should be considered for this property leaving<br>some room for threads not associated with an HTTP request.<br> |
| __Default__ | 0 |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.websockets.grizzly.remote.maxDelayDuration |
|:----------------|:--------|
| __Description__ | The time in milliseconds a message (that is supposed to be transferred to a remote cluster member)<br>is at max. queued in buffer to await & aggregate equal messages that arrive during that time.<br>So, even if there was an equal message recently, message is flushed from queue to avoid holding back<br>a message forever in case there are frequent equal messages.<br> |
| __Default__ | 3000 |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Websockets.html">Websockets</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | websockets.properties |

---
| __Key__ | com.openexchange.nosql.cassandra.maximumLocalConnectionsPerNode |
|:----------------|:--------|
| __Description__ | Defines the amount of maximum connections to the local Datacenter |
| __Default__ | 10 |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.nosql.cassandra.minimumLocalConnectionsPerNode |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | cassandra.properties |

---
| __Key__ | com.openexchange.nosql.cassandra.maximumRemoteConnectionsPerNode |
|:----------------|:--------|
| __Description__ | Defines the throttling of concurrent requests per connection on local (on the same datacenter) nodes. <br>For Cassandra clusters that use a protocol v2 and below, there is no reason to throttle. It should be set to 128 (the max) <br>For Cassandra clusters that use a protocol v3 and up, it is set by default to 1024. These low defaults were chosen so that the default configuration for protocol v2 and v3 allow the same total number of simultaneous requests (to avoid bad surprises when clients migrate from v2 to v3). This threshold can be raised, or even set it to the max which is 32768 for LOCAL nodes.<br>Note that that high values will give clients more bandwidth and therefore put more pressure on the cluster. This might require some tuning, especially with many clients. <br> |
| __Default__ | 1024 |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.nosql.cassandra.maximumRequestsPerLocalConnection |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | cassandra.properties |

---
| __Key__ | com.openexchange.nosql.cassandra.maximumRemoteConnectionsPerNode |
|:----------------|:--------|
| __Description__ | Defines the throttling of concurrent requests per connection on remote (on a different datacenter) nodes.<br>For Cassandra clusters that use a protocol v2 and below, there is no reason to throttle. It should be set to 128 (the max) <br>For Cassandra clusters that use a protocol v3 and up, it is set by default to 256. These low defaults were chosen so that the default configuration for protocol v2 and v3 allow the same total number of simultaneous requests (to avoid bad surprises when clients migrate from v2 to v3). This threshold can be raised, or even set it to the max which is 2000 for REMOTE nodes.<br>Note that that high values will give clients more bandwidth and therefore put more pressure on the cluster. This might require some tuning, especially with many clients. <br> |
| __Default__ | 256 |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.nosql.cassandra.maximumRequestsPerRemoteConnection |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | cassandra.properties |

---
| __Key__ | com.openexchange.nosql.cassandra.maximumRemoteConnectionsPerNode |
|:----------------|:--------|
| __Description__ | When the CassandraService tries to send a request to a host, it will first try to acquire a connection from this host's pool. If the pool is busy (i.e. all connections are already handling their maximum number of in flight requests), the acquisition attempt gets enqueued until a connection becomes available again. <br>If the queue has already reached its limit, further attempts to acquire a connection will be rejected immediately: the CassandraService will move on and try to acquire a connection from the next host's pool. The limit can be set to 0 to disable queueing entirely. <br>If all hosts are busy with a full queue, the request will fail with a <code&#124;NoHostAvailableException</code&#124;.<br> |
| __Default__ | 256 |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.nosql.cassandra.acquistionQueueMaxSize |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | cassandra.properties |

---
| __Key__ | com.openexchange.pns.maxProcessorTasks |
|:----------------|:--------|
| __Description__ | Specifies the buffer size for due notifications that were transferred from buffering queue to<br>processing queue.<br> |
| __Default__ | 65536 |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | pns.properties |

---
