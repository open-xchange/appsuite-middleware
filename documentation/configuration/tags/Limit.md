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
| __Key__ | com.openexchange.dataretention.rotateLength |
|:----------------|:--------|
| __Description__ | Specifies the max. output resource's length (in bytes) before it gets rotated.<br>This option is only useful for implementations which output data to a file or<br>to any limited resource. This value should have a reasonable size since multiple<br>write accesses may occur at same time. Therefore small sizes (<= 200KB) cannot<br>be guaranteed being obeyed.<br>Moreover it is only an approximate limit which can vary about 8KB.<br>A value less than or equal to zero means no rotation.<br> |
| __Default__ | 0 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Data_Retention.html">Data Retention</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | dataretention.properties |

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
| __Key__ | com.openexchange.client.onboarding.sms.ratelimit |
|:----------------|:--------|
| __Description__ | Define the time (in milliseconds) which must pass by before a new sms can be sent<br>A value of 0 disables the limit.<br> |
| __Default__ | 0 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.client.onboarding.plist.pkcs12store.filename |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMS.html">SMS</a> |
| __File__ | client-onboarding.properties |

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
| __Key__ | com.openexchange.drive.cleaner.maxAge |
|:----------------|:--------|
| __Description__ | Defines the maximum age of files and directories to be kept inside the<br>temporary ".drive" folder. Files or directories that were last modified<br>before the configured age are deleted during the next run of the cleaner<br>process. The value can be defined using units of measurement: "D" (=days),<br>"W" (=weeks) and "H" (=hours).<br> |
| __Default__ | 1D |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.checksum.cleaner.maxAge |
|:----------------|:--------|
| __Description__ | Defines the timespan after which an unused checksum should be removed from <br>the database cache.<br>The value can be defined using units of measurement: "D" (=days), <br>"W" (=weeks) and "H" (=hours) with a minimum of "1D" (one day). <br> |
| __Default__ | 4W |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.maxBandwidth |
|:----------------|:--------|
| __Description__ | Allows to limit the maximum used bandwidth for all downloads. If<br>configured, downloads via the drive module handled by this backend node will<br>not exceed the configured bandwidth. The available bandwidth is defined as<br>the number of allowed bytes per second, where the byte value can be<br>specified with one of the units "B" (bytes), "kB" (kilobyte), "MB"<br>(Megabyte) or "GB" (Gigabyte), e.g. "10 MB". Must fit into the "Integer"<br>range, i.e. the configured number of bytes has to be be smaller than 2^31.<br>"-1" means no limitations.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.maxBandwidthPerClient |
|:----------------|:--------|
| __Description__ | Allows to limit the maximum used bandwidth for client downloads within the<br>same session. If configured, downloads originating in the same session via<br>the drive module handled by this backend node will not exceed the<br>configured bandwidth. The available bandwidth is defined as the number of<br>allowed bytes per second, where the byte value can be specified with one of<br>the units "B" (bytes), "kB" (kilobyte), "MB" (Megabyte) or "GB" (Gigabyte),<br>e.g. "500 kB". Must fit into the "Integer" range, i.e. the configured<br>number of bytes has to be be smaller than 2^31. <br>"-1" means no limitations.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.maxConcurrentSyncOperations |
|:----------------|:--------|
| __Description__ | Specifies the maximum allowed number of synchronization operations, i.e.<br>all requests to the "drive" module apart from up- and downloads, that the<br>server accepts concurrently. While the limit is reached, further<br>synchronization requests are rejected in a HTTP 503 manner (service<br>unavailable), and the client is instructed to try again at a later time.<br>"-1" means no limitations.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.maxDirectories |
|:----------------|:--------|
| __Description__ | Defines the maximum number of synchronizable directories per root folder. A<br>value of "-1" disables the limitation.<br> |
| __Default__ | 65535 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.maxFilesPerDirectory |
|:----------------|:--------|
| __Description__ | Defines the maximum number of synchronizable files per root folder. A<br>value of "-1" disables the limitation.<br> |
| __Default__ | 65535 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | drive.properties |

---
| __Key__ | MAX_UPLOAD_SIZE |
|:----------------|:--------|
| __Description__ | If the sum of all uploaded files (for contacts, appointments or tasks) in one request is larger than this value,<br>the upload will be rejected. If this value is not set or -1, the more general MAX_UPLOAD_SIZE configured in<br>server.properties will be used. If that value is 0 uploads will be unrestricted.<br>The size is in Bytes.<br> |
| __Default__ | 10485760 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Attachment.html">Attachment</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contacts.html">Contacts</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Tasks.html">Tasks</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointments.html">Appointments</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | attachment.properties |

---
| __Key__ | MAX_PRE_FETCH |
|:----------------|:--------|
| __Description__ | By enabling the option CACHED_ITERATOR_FAST_FETCH you<br>can define the numbers of pre fetched results with<br>the parameter MAX_PRE_FETCH.<br><br>This means that MAX_PRE_FETCH results are gathered in one<br>SQL query instead of MAX_PRE_FETCH single SQL queries.<br>Normally higher values result in more performance if this<br>option is enabled.<br> |
| __Default__ | 20 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | CACHED_ITERATOR_FAST_FETCH |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Calendar.html">Calendar</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | calendar.properties |

---
| __Key__ | MAX_OPERATIONS_IN_RECURRENCE_CALCULATIONS |
|:----------------|:--------|
| __Description__ | This options specifies a maximum count of loop iterations<br>for a given recurrence pattern. When this limit is reached<br>the server stops processing the recurrence pattern and spews<br>out lots of error information. A value equal to or less than<br>zero omits this property; meaning no limit on processing the<br>recurrence pattern.<br> |
| __Default__ | 49950 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Calendar.html">Calendar</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | calendar.properties |

---
| __Key__ | com.openexchange.calendar.seriesconflictlimit |
|:----------------|:--------|
| __Description__ | This boolean option switches on/off the limitation for the<br>conflict search for a series to 1 year in the future. This<br>means, that a new/changed series will not conflict with<br>appointments which are later than one year after the<br>creation/change of the appointment.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Calendar.html">Calendar</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | calendar.properties |

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
| __Key__ | com.openexchange.caldav.interval.start |
|:----------------|:--------|
| __Description__ | Appointments and tasks are available via the CalDAV interface if they fall <br>into a configurable timeframe. This value specifies the start time of this <br>interval, i.e. how far past appointments should be considered. More formal, <br>this value defines the negative offset relative to the current date <br>representing the minimum end time of appointments to be synchronized.<br>Possible values are "one_month", "one_year" and "six_months". <br> |
| __Default__ | one_month |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CalDAV.html">CalDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | caldav.properties |

---
| __Key__ | com.openexchange.caldav.interval.end |
|:----------------|:--------|
| __Description__ | Appointments and tasks are available via the CalDAV interface if they fall <br>into a configurable timeframe. This value specifies the end time of this<br>interval, i.e. how far future appointments should be considered. More <br>formal, this value defines the positive offset relative to the current date <br>representing the maximum start time of appointments to be synchronized.<br>Possible values are "one_year" and "two_years".<br> |
| __Default__ | one_year |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CalDAV.html">CalDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | caldav.properties |

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
| __Key__ | com.openexchange.audit.logging.AuditFileHandler.limit |
|:----------------|:--------|
| __Description__ | The maximum file size.<br> |
| __Default__ | 2097152 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Audit.html">Audit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | audit.properties |

---
