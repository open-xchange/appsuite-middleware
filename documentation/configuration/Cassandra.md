# Cassandra

| __Key__ | com.openexchange.nosql.cassandra.clusterName |
|:----------------|:--------|
| __Description__ | Defines the name of the Cassandra cluster. Technically this name does not correlate with the name configured in the real Cassandra cluster, but it's rather used to distinguish exposed JMX metrics when multiple Cluster instances live in the same JVM <br> |
| __Default__ | ox |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | cassandra.properties |

---
| __Key__ | com.openexchange.nosql.cassandra.clusterContactPoints |
|:----------------|:--------|
| __Description__ | Defines the Cassandra seed node(s) as a comma separated list<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | cassandra.properties |

---
| __Key__ | com.openexchange.nosql.cassandra.port |
|:----------------|:--------|
| __Description__ | Defines the port on which the Cassandra server is running<br> |
| __Default__ | 9042 |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | cassandra.properties |

---
| __Key__ | com.openexchange.nosql.cassandra.loadBalancingPolicy |
|:----------------|:--------|
| __Description__ | Defines load balancing policy to use for the cluster. There are three load balancing policies to choose from: <code|RoundRobin</code|, <code|DCAwareRoundRobin</code| and <code|DCTokenAwareRoundRobin</code|.<br><b|<u|RoundRobin</u|</b|<br>A Round-robin load balancing policy. <br>This policy queries nodes in a round-robin fashion. For a given query, if an host fail, the next one (following the round-robin order) is tried, until all hosts have been tried. <br>This policy is not datacenter aware and will include every known Cassandra host in its round robin algorithm. If you use multiple datacenter this will be inefficient and you will want to use the <code|DCAwareRoundRobin</code| load balancing policy instead. <br><b|<u|DCAwareRoundRobin</u|</b|<br>A data-center aware Round-robin load balancing policy. <br>This policy provides round-robin queries over the node of the local data center. It also includes in the query plans returned a configurable number of hosts in the remote data centers, but those are always tried after the local nodes. In other words, this policy guarantees that no host in a remote data center will be queried unless no host in the local data center can be reached. <br>If used with a single data center, this policy is equivalent to the <code|RoundRobin</code|, but its DC awareness incurs a slight overhead so the latter should be preferred to this policy in that case. <br><b|<u|DCTokenAwareRoundRobin</u|</b|<br>Same as the <code|DCAwareRoundRobin</code| load balancing policy but with added token awareness. <br> |
| __Default__ | RoundRobin |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | cassandra.properties |

---
| __Key__ | com.openexchange.nosql.cassandra.retryPolicy |
|:----------------|:--------|
| __Description__ | A policy that defines a default behaviour to adopt when a request fails. There are three retry policies to choose from: <code|defaultRetryPolicy</code|, <code|downgradingConsistencyRetryPolicy</code| and <code|fallthroughRetryPolicy</code| <br><b|<u|defaultRetryPolicy</u|</b|<br>This policy retries queries in only two cases: <ul|<li|On a read timeout, if enough replicas replied but data was not retrieved.</li|<li|On a write timeout, if we timeout while writing the distributed log used by batch statements.</li|</ul|<br>This retry policy is conservative in that it will never retry with a different consistency level than the one of the initial operation. <br>In some cases, it may be convenient to use a more aggressive retry policy like <code|downgradingConsistencyRetryPolicy</code|. <br><u|downgradingConsistencyRetryPolicy</u|<br>A retry policy that sometimes retries with a lower consistency level than the one initially requested. <br><b|BEWARE</b|: this policy may retry queries using a lower consistency level than the one initially requested. By doing so, it may break consistency guarantees. In other words, if you use this retry policy, there are cases where a read at QUORUM may NOT see a preceding write at QUORUM. Do not use this policy unless you have understood the cases where this can happen and are ok with that. It is also highly recommended to always enable the <code|logRetryPolicy</code| to log the occurrences of such consistency breaks. <br><b|<u|fallthroughRetryPolicy</u|</b|<br>A retry policy that never retries (nor ignores). <br> |
| __Default__ | defaultRetryPolicy |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | cassandra.properties |

---
| __Key__ | com.openexchange.nosql.cassandra.logRetryPolicy |
|:----------------|:--------|
| __Description__ | Logs the retry decision of the policy<br> |
| __Default__ | false |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | cassandra.properties |

---
| __Key__ | com.openexchange.nosql.cassandra.enableQueryLogger |
|:----------------|:--------|
| __Description__ | Enables the query logger which logs all executed statements<br> |
| __Default__ | false |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | cassandra.properties |

---
| __Key__ | com.openexchange.nosql.cassandra.queryLatencyThreshold |
|:----------------|:--------|
| __Description__ | Defines the latency threshold in milliseconds beyond which queries are considered 'slow' and logged as such by the Cassandra service. Used in conjunction with the <code|enableQueryLogger</code| property <br> |
| __Default__ | 5000 |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | cassandra.properties |

---
| __Key__ | com.openexchange.nosql.cassandra.poolingHeartbeat |
|:----------------|:--------|
| __Description__ | Defines the amount of time (in seconds) for connection keepalive in the form of a heartbeat. When a connection has been idle for the given amount of time, the Cassandra service will simulate activity by writing a dummy request to it (by sending an <code|OPTIONS</code| message). <br>To disable heartbeat, set the interval to 0. <br> |
| __Default__ | 30 |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | cassandra.properties |

---
| __Key__ | com.openexchange.nosql.cassandra.minimumLocalConnectionsPerNode |
|:----------------|:--------|
| __Description__ | The Cassandra service's connection pools have a variable size, which gets adjusted automatically depending on the current load. There will always be at least a minimum number of connections, and at most a maximum number. These values can be configured independently by host distance (the distance is determined by your <code|loadBalancingPolicy</code|, and will generally indicate whether a host is in the same datacenter or not).<br>This property defines the minimum connections to the local datacenter<br> |
| __Default__ | 4 |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | cassandra.properties |

---
| __Key__ | com.openexchange.nosql.cassandra.maximumLocalConnectionsPerNode |
|:----------------|:--------|
| __Description__ | Defines the amount of maximum connections to the local Datacenter |
| __Default__ | 10 |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.nosql.cassandra.minimumLocalConnectionsPerNode |
| __File__ | cassandra.properties |

---
| __Key__ | com.openexchange.nosql.cassandra.minimumRemoteConnectionsPerNode |
|:----------------|:--------|
| __Description__ | Defines the amount of minimum connections to the remote Datacenter |
| __Default__ | 2 |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.nosql.cassandra.minimumLocalConnectionsPerNode |
| __File__ | cassandra.properties |

---
| __Key__ | com.openexchange.nosql.cassandra.maximumRemoteConnectionsPerNode |
|:----------------|:--------|
| __Description__ | Defines the amount of maximum connections to the remote Datacenter |
| __Default__ | 4 |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.nosql.cassandra.minimumLocalConnectionsPerNode |
| __File__ | cassandra.properties |

---
| __Key__ | com.openexchange.nosql.cassandra.maximumRemoteConnectionsPerNode |
|:----------------|:--------|
| __Description__ | When activity goes down, the driver will "trash" connections if the maximum number of requests in a 10 second time period can be satisfied by less than the number of connections opened. Trashed connections are kept open but do not accept new requests. After the given timeout, trashed connections are closed and removed. If during that idle period activity increases again, those connections will be resurrected back into the active pool and reused. <br> |
| __Default__ | 120 |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.nosql.cassandra.idleConnectionTrashTimeout |
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
| __File__ | cassandra.properties |

---
| __Key__ | com.openexchange.nosql.cassandra.maximumRemoteConnectionsPerNode |
|:----------------|:--------|
| __Description__ | When the CassandraService tries to send a request to a host, it will first try to acquire a connection from this host's pool. If the pool is busy (i.e. all connections are already handling their maximum number of in flight requests), the acquisition attempt gets enqueued until a connection becomes available again. <br>If the queue has already reached its limit, further attempts to acquire a connection will be rejected immediately: the CassandraService will move on and try to acquire a connection from the next host's pool. The limit can be set to 0 to disable queueing entirely. <br>If all hosts are busy with a full queue, the request will fail with a <code|NoHostAvailableException</code|.<br> |
| __Default__ | 256 |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.nosql.cassandra.acquistionQueueMaxSize |
| __File__ | cassandra.properties |

---
| __Key__ | com.openexchange.nosql.cassandra.connectTimeout |
|:----------------|:--------|
| __Description__ | Specifies the connect timeout in milliseconds<br> |
| __Default__ | 5000 |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | cassandra.properties |

---
| __Key__ | com.openexchange.nosql.cassandra.readTimeout |
|:----------------|:--------|
| __Description__ | Specifies the read timeout in milliseconds<br> |
| __Default__ | 12000 |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | cassandra.properties |

---
