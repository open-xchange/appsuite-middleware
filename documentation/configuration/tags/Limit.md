---
title: Limit
---

This page shows all properties with the tag: Limit

| __Key__ | com.openexchange.import.ical.limit |
|:----------------|:--------|
| __Description__ | Sets a limit on how many entries a single import of ical data may contain.<br>Note that this limit applies for each type, so you can have, for example, 10000 VEVENTS and 10000 VFREEBUSY entries in a single file. <br>-1 means unlimited.<br> |
| __Default__ | 10000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Import.html">Import</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | import.properties.in |

---
| __Key__ | com.openexchange.import.contacts.limit |
|:----------------|:--------|
| __Description__ | Sets the limit on how many contacts can be imported at once.<br>-1 means unlimited.<br> |
| __Default__ | -1 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Import.html">Import</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | import.properties.in |

---
| __Key__ | com.openexchange.snippet.quota.limit |
|:----------------|:--------|
| __Description__ | Specify the maximum number of snippets that are allowed being created by a single user.<br>A value of less than 0 (zero) means unlimited.<br> |
| __Default__ | -1 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Snippets.html">Snippets</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | snippets.properties |

---
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
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.http.grizzly.readTimeoutMillis |
|:----------------|:--------|
| __Description__ | Specifies the read timeout, in milliseconds. A timeout of zero is interpreted as an infinite timeout.<br> |
| __Default__ | 60000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.http.grizzly.writeTimeoutMillis |
|:----------------|:--------|
| __Description__ | Specifies the write timeout, in milliseconds. A timeout of zero is interpreted as an infinite timeout.<br> |
| __Default__ | 60000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.http.grizzly.maxHttpHeaderSize |
|:----------------|:--------|
| __Description__ | The maximum header size for an HTTP request in bytes. Make sure to increase<br>this value for all components of your infrastructure when you are forced to<br>deal with enormous headers. For Apache as our default balancer see<br>http://httpd.apache.org/docs/current/mod/core.html#limitrequestfieldsize<br> |
| __Default__ | 8192 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.threadpool.maximumPoolSize |
|:----------------|:--------|
| __Description__ | The maximum number of threads to allow in the pool.<br>The max. integer value of 2^31 - 1 is considered as unlimited max. number of threads.<br> |
| __Default__ | 2147483647 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Threadpool.html">Threadpool</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | threadpool.properties |

---
| __Key__ | com.openexchange.threadpool.keepAliveTime |
|:----------------|:--------|
| __Description__ | When the number of threads is greater than the core, this is the maximum<br>time (in milliseconds) that excess idle threads will wait for new tasks before terminating.<br> |
| __Default__ | 60000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Threadpool.html">Threadpool</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | threadpool.properties |

---
| __Key__ | com.openexchange.mail.transport.referencedPartLimit |
|:----------------|:--------|
| __Description__ | Define the limit in bytes for keeping an internal copy of a referenced<br>MIME message's part when sending a mail. If a part exceeds this limit<br>a temporary file is created holding part's copy.<br> |
| __Default__ | 1048576 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Transport.html">Transport</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | transport.properties |

---
| __Key__ | com.openexchange.user.maxClientCount |
|:----------------|:--------|
| __Description__ | Specify the max. allowed number of client identifiers stored/tracked per user.<br>A value equal to or less than zero means unlimited.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | user.properties |

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
| __Key__ | com.openexchange.quota.calendar |
|:----------------|:--------|
| __Description__ | Specifies the quota for the number of appointments that are allowed being created within a single context (tenant-wise scope).<br><br>The purpose of this quota is to define a rough upper limit that is unlikely being reached during normal operation.<br>Therefore it is rather supposed to prevent from excessive item creation (e.g. a synchronizing client running mad),<br>but not intended to have a fine-grained quota setting. Thus exceeding that quota limitation will cause an appropriate<br>exception being thrown, denying to further create any appointment in affected context.<br> |
| __Default__ | 250000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | quota.properties |

---
| __Key__ | com.openexchange.quota.task |
|:----------------|:--------|
| __Description__ | Specifies the quota for the number of tasks that are allowed being created within a single context (tenant-wise scope).<br><br>The purpose of this quota is to define a rough upper limit that is unlikely being reached during normal operation.<br>Therefore it is rather supposed to prevent from excessive item creation (e.g. a synchronizing client running mad),<br>but not intended to have a fine-grained quota setting. Thus exceeding that quota limitation will cause an appropriate<br>exception being thrown, denying to further create any task in affected context.<br> |
| __Default__ | 250000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Task.html">Task</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | quota.properties |

---
| __Key__ | com.openexchange.quota.contact |
|:----------------|:--------|
| __Description__ | Specifies the quota for the number of contacts that are allowed being created within a single context (tenant-wise scope).<br><br>The purpose of this quota is to define a rough upper limit that is unlikely being reached during normal operation.<br>Therefore it is rather supposed to prevent from excessive item creation (e.g. a synchronizing client running mad),<br>but not intended to have a fine-grained quota setting. Thus exceeding that quota limitation will cause an appropriate<br>exception being thrown, denying to further create any contact in affected context.<br> |
| __Default__ | 250000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | quota.properties |

---
| __Key__ | com.openexchange.quota.infostore |
|:----------------|:--------|
| __Description__ | Specifies the quota for the number of documents that are allowed being created within a single context (tenant-wise scope).<br><br>The purpose of this quota is to define a rough upper limit that is unlikely being reached during normal operation.<br>Therefore it is rather supposed to prevent from excessive item creation (e.g. a synchronizing client running mad),<br>but not intended to have a fine-grained quota setting. Thus exceeding that quota limitation will cause an appropriate<br>exception being thrown, denying to further create any document in affected context.<br> |
| __Default__ | 250000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Infostore.html">Infostore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | quota.properties |

---
| __Key__ | com.openexchange.quota.attachment |
|:----------------|:--------|
| __Description__ | Specifies the quota for the number of attachments bound to PIM objects that are allowed being created within a single context (tenant-wise scope).<br><br>The purpose of this quota is to define a rough upper limit that is unlikely being reached during normal operation.<br>Therefore it is rather supposed to prevent from excessive item creation (e.g. a synchronizing client running mad),<br>but not intended to have a fine-grained quota setting. Thus exceeding that quota limitation will cause an appropriate<br>exception being thrown, denying to further create any attachment in affected context.<br> |
| __Default__ | 250000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Attachment.html">Attachment</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | quota.properties |

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
| __Key__ | com.openexchange.jolokia.maxDepth |
|:----------------|:--------|
| __Description__ | Maximum depth when traversing bean properties. If set to 0, depth checking is disabled.<br> |
| __Default__ | 0 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Jolokia.html">Jolokia</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | jolokia.properties |

---
| __Key__ | com.openexchange.jolokia.maxObjects |
|:----------------|:--------|
| __Description__ | Maximum number of objects which are traversed when serializing a single response.<br>Use this as an airbag to avoid boosting your memory and network traffic. Nevertheless, when set to 0 no limit is imposed.<br> |
| __Default__ | 100000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Jolokia.html">Jolokia</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | jolokia.properties |

---
| __Key__ | com.openexchange.download.limit.enabled |
|:----------------|:--------|
| __Description__ | If the feature is disabled (in general or for guests/links) no downloads will be tracked which means after<br>activation each guest/link starts with used counts/size 0.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Download.html">Download</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a> |
| __File__ | download.properties |

---
| __Key__ | com.openexchange.download.limit.timeFrame.guests |
|:----------------|:--------|
| __Description__ | Specify the limit (in milliseconds) time window in which to track (and possibly <br>deny) incoming download requests for known (guests) guest users.<br>That rate limit acts like a sliding window time frame; meaning that it considers only<br>requests that fit into time windows specified through "com.openexchange.download.limit.guests.timeFrame" <br>from current time stamp:<br>window-end := $now<br>window-start := $window-end - $timeFrame<br>If you only want to specify only one limit (size or count) you have to set a time frame and specify the desired<br> |
| __Default__ | 3600000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Download.html">Download</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a> |
| __File__ | download.properties |

---
| __Key__ | com.openexchange.download.limit.timeFrame.links |
|:----------------|:--------|
| __Description__ | Specify the limit (in milliseconds) time window in which to track (and possibly <br>deny) incoming download requests for anonymous (links) guest users.<br>That rate limit acts like a sliding window time frame; meaning that it considers only<br>requests that fit into time windows specified through "com.openexchange.download.limit.links.timeFrame" <br>from current time stamp:<br>window-end := $now<br>window-start := $window-end - $timeFrame<br>If you only want to specify only one limit (size or count) you have to set a time frame and specify the desired<br> |
| __Default__ | 3600000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Download.html">Download</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a> |
| __File__ | download.properties |

---
| __Key__ | com.openexchange.download.limit.size.guests |
|:----------------|:--------|
| __Description__ | Specify the download size limit<br>A guest (link or known) that exceeds that limit will receive an error<br>Default is 1073741824 (1 GB) bytes per $timeFrame.<br>To disable the size check set value to 0<br> |
| __Default__ | 1073741824 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Download.html">Download</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a> |
| __File__ | download.properties |

---
| __Key__ | com.openexchange.download.limit.size.links |
|:----------------|:--------|
| __Description__ | Specify the download size limit<br>A guest (link or known) that exceeds that limit will receive an error<br>Default is 1073741824 (1 GB) bytes per $timeFrame.<br>To disable the size check set value to 0<br> |
| __Default__ | 1073741824 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Download.html">Download</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a> |
| __File__ | download.properties |

---
| __Key__ | com.openexchange.download.limit.count.guests |
|:----------------|:--------|
| __Description__ | Default is 100 downloads per $timeFrame.<br>A guest (link or known)  that exceeds that limit will receive an error<br>To disable the count check set value to 0<br> |
| __Default__ | 100 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Download.html">Download</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a> |
| __File__ | download.properties |

---
| __Key__ | com.openexchange.download.limit.count.links |
|:----------------|:--------|
| __Description__ | Default is 100 downloads per $timeFrame.<br>A guest (link or known)  that exceeds that limit will receive an error<br>To disable the count check set value to 0<br> |
| __Default__ | 100 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Download.html">Download</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a> |
| __File__ | download.properties |

---
| __Key__ | com.openexchange.soap.cxf.entityExpansionLimit |
|:----------------|:--------|
| __Description__ | Java platform limits the number of entity expansions that are allowed for a single XML document.<br>Default is 128000, which is considered to be a pretty large number for any real life application.<br><br>However, if any application does need to have a higher limit, this property (which maps to 'entityExpansionLimit' system property)<br>can be increased to the desired size. Setting it to 0 (zero) means unlimited.<br> |
| __Default__ | 128000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Soap.html">Soap</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | soap-cxf.properties |

---
| __Key__ | com.openexchange.soap.cxf.disableAddressUpdates |
|:----------------|:--------|
| __Description__ | This is a workaround for the known side-effect in CXF 2.7.x described in CXF-5737 issue (https://issues.apache.org/jira/browse/CXF-5737)<br>The endpoint address gets manipulating after accessing it via multiple aliases.<br>This is disabled by default in the upcoming versions of CXF.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Soap.html">Soap</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | soap-cxf.properties |

---
| __Key__ | MAX_UPLOAD_SIZE |
|:----------------|:--------|
| __Description__ | If the sum of all uploaded files (for contacts, appointments or tasks) in one request is larger than this value,<br>the upload will be rejected. If this value is not set or -1, the more general MAX_UPLOAD_SIZE configured in<br>server.properties will be used. If that value is 0 uploads will be unrestricted.<br>The size is in Bytes.<br> |
| __Default__ | 10485760 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Attachment.html">Attachment</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Tasks.html">Tasks</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | attachment.properties |

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
| __Key__ | MAX_PRE_FETCH |
|:----------------|:--------|
| __Description__ | By enabling the option CACHED_ITERATOR_FAST_FETCH you<br>can define the numbers of pre fetched results with<br>the parameter MAX_PRE_FETCH.<br><br>This means that MAX_PRE_FETCH results are gathered in one<br>SQL query instead of MAX_PRE_FETCH single SQL queries.<br>Normally higher values result in more performance if this<br>option is enabled.<br> |
| __Default__ | 20 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | CACHED_ITERATOR_FAST_FETCH |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Calendar.html">Calendar</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Cache.html">Cache</a> |
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
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Calendar.html">Calendar</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a> |
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
| __Description__ | Defines the throttling of concurrent requests per connection on local (on the same datacenter) nodes. <br>For Cassandra clusters that use a protocol v2 and below, there is no reason to throttle. It should be set to 128 (the max) <br>For Cassandra clusters that use a protocol v3 and up, it is set by default to 1024. <br>These low defaults were chosen so that the default configuration for protocol v2 and v3 allow the same total number of simultaneous requests <br>(to avoid bad surprises when clients migrate from v2 to v3). This threshold can be raised, or even set it to the max which is 32768 for LOCAL nodes.<br>Note that that high values will give clients more bandwidth and therefore put more pressure on the cluster. This might require some tuning, especially with many clients. <br> |
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
| __Description__ | Defines the throttling of concurrent requests per connection on remote (on a different datacenter) nodes.<br>For Cassandra clusters that use a protocol v2 and below, there is no reason to throttle. It should be set to 128 (the max) <br>For Cassandra clusters that use a protocol v3 and up, it is set by default to 256. <br>These low defaults were chosen so that the default configuration for protocol v2 and v3 allow the same total number of simultaneous requests <br>(to avoid bad surprises when clients migrate from v2 to v3). This threshold can be raised, or even set it to the max which is 2000 for REMOTE nodes.<br>Note that that high values will give clients more bandwidth and therefore put more pressure on the cluster. This might require some tuning, especially with many clients. <br> |
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
| __Description__ | When the CassandraService tries to send a request to a host, it will first try to acquire a connection from this host's pool.<br>If the pool is busy (i.e. all connections are already handling their maximum number of in flight requests), the acquisition attempt gets enqueued until a connection becomes available again. <br>If the queue has already reached its limit, further attempts to acquire a connection will be rejected immediately: <br>the CassandraService will move on and try to acquire a connection from the next host's pool. The limit can be set to 0 to disable queueing entirely. <br>If all hosts are busy with a full queue, the request will fail with a <code>NoHostAvailableException</code>.<br> |
| __Default__ | 256 |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.nosql.cassandra.acquistionQueueMaxSize |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | cassandra.properties |

---
| __Key__ | com.openexchange.messaging.rss.feed.size |
|:----------------|:--------|
| __Description__ | Defines the maximum feed size for an RSS feed in bytes.<br> |
| __Default__ | 4194304 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/RSS.html">RSS</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | rssmessaging.properties |

---
| __Key__ | com.openexchange.caldav.interval.start |
|:----------------|:--------|
| __Description__ | Appointments and tasks are available via the CalDAV interface if they fall <br>into a configurable timeframe. This value specifies the start time of this <br>interval, i.e. how far past appointments should be considered. More formal, <br>this value defines the negative offset relative to the current date <br>representing the minimum end time of appointments to be synchronized.<br>Possible values are "one_month", "one_year" and "six_months". <br> |
| __Default__ | one_month |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CalDAV.html">CalDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a> |
| __File__ | caldav.properties |

---
| __Key__ | com.openexchange.caldav.interval.end |
|:----------------|:--------|
| __Description__ | Appointments and tasks are available via the CalDAV interface if they fall <br>into a configurable timeframe. This value specifies the end time of this<br>interval, i.e. how far future appointments should be considered. More <br>formal, this value defines the positive offset relative to the current date <br>representing the maximum start time of appointments to be synchronized.<br>Possible values are "one_year" and "two_years".<br> |
| __Default__ | one_year |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CalDAV.html">CalDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a> |
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
| __Key__ | com.openexchange.push.ms.maxDelayDuration |
|:----------------|:--------|
| __Description__ | The maximum time in milliseconds a push object may be delayed before finally pushing it to the clients.<br> |
| __Default__ | 600000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | push-ms.properties |

---
| __Key__ | com.openexchange.sms.sipgate.maxlength |
|:----------------|:--------|
| __Description__ | Max message length. 460 characters is sipgate's maximum.<br> |
| __Default__ | 460 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Sipgate.html">Sipgate</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMS.html">SMS</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | sipgate.properties |

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
| __Key__ | com.openexchange.log.audit.slf4j.file.size |
|:----------------|:--------|
| __Description__ | Specifies the max. file size to use.<br> |
| __Default__ | 2097152 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.log.audit.slf4j.file.location |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Audit.html">Audit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Logging.html">Logging</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | slf4j-auditlog.properties |

---
| __Key__ | com.openexchange.log.audit.slf4j.file.count |
|:----------------|:--------|
| __Description__ | Specifies the max. number of files to use for rotation.<br> |
| __Default__ | 99 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.log.audit.slf4j.file.location |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Audit.html">Audit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Logging.html">Logging</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | slf4j-auditlog.properties |

---
