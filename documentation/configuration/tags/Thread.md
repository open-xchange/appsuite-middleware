---
title: Thread
---

This page shows all properties with the tag: Thread

| __Key__ | com.openexchange.mail.mailAccessCacheShrinkerSeconds |
|:----------------|:--------|
| __Description__ | Define the interval seconds of the mail access cache's shrinker thread<br> |
| __Default__ | 3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Thread.html">Thread</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.mailAccessCacheIdleSeconds |
|:----------------|:--------|
| __Description__ | Define the idle seconds a mail access may reside in mail access cache before it is removed by shrinker thread.<br> |
| __Default__ | 4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Thread.html">Thread</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.report.appsuite.maxThreadPoolSize |
|:----------------|:--------|
| __Description__ | Number of threads allowed to work on the report at the same time.<br> |
| __Default__ | 20 |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Report.html">Report</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Thread.html">Thread</a> |
| __File__ | reportserialization.properties |

---
| __Key__ | com.openexchange.report.appsuite.threadPriority |
|:----------------|:--------|
| __Description__ | The priority that threads, working on the report have. Allowed value range is 1-10. 1 is the lowest, 10 the highest priority.<br> |
| __Default__ | 1 |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Report.html">Report</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Thread.html">Thread</a> |
| __File__ | reportserialization.properties |

---
| __Key__ | com.openexchange.push.malpoll.concurrentglobal |
|:----------------|:--------|
| __Description__ | Whether the tasks executed by global timer are executed concurrently<br>or by calling timer's thread.<br>Note: This property only has effect if "com.openexchange.push.malpoll.global"<br>is set to "true"<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.push.malpoll.global |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/MAL_Poll.html">MAL Poll</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Thread.html">Thread</a> |
| __File__ | malpoll.properties |

---
| __Key__ | cleanerInterval |
|:----------------|:--------|
| __Description__ | Timeinterval of cleaner thread in milliseconds. <br>This thread removes idle timed out database connections and <br>removes not used database connection pools after each cleanerInterval.<br> |
| __Default__ | 10000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Thread.html">Thread</a> |
| __File__ | configdb.properties |

---
| __Key__ | maxWait |
|:----------------|:--------|
| __Description__ | If exhaustedAction is set to BLOCK, a thread will not wait for more than <br>maxWait milliseconds.<br> |
| __Default__ | 10000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | exhaustedAction |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Thread.html">Thread</a> |
| __File__ | configdb.properties |

---
| __Key__ | testThreads |
|:----------------|:--------|
| __Description__ | If testThreads is set to true, more information is logged to the Open-Xchange<br>log files about database connections.  If this option is enabled the<br>performance may degrade dramatically. The JVM has to generate then a lot of<br>method call stack dumps.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Logging.html">Logging</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Thread.html">Thread</a> |
| __File__ | configdb.properties |

---
| __Key__ | com.openexchange.report.appsuite.maxThreadPoolSize |
|:----------------|:--------|
| __Description__ | How many threads can be used for report processing.<br> |
| __Default__ | 20 |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Report.html">Report</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Serialization.html">Serialization</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Thread.html">Thread</a> |
| __File__ | reportclient.properties |

---
| __Key__ | com.openexchange.report.appsuite.threadPriority |
|:----------------|:--------|
| __Description__ | Which thread priority do the processing threads have.<br> |
| __Default__ | 1 |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Report.html">Report</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Serialization.html">Serialization</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Thread.html">Thread</a> |
| __File__ | reportclient.properties |

---
| __Key__ | com.openexchange.threadpool.corePoolSize |
|:----------------|:--------|
| __Description__ | The number of threads to keep in the pool, even if they are idle.<br>If unsure follow this rule: Number of CPUs + 1.<br> |
| __Default__ | 3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Threadpool.html">Threadpool</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Thread.html">Thread</a> |
| __File__ | threadpool.properties |

---
| __Key__ | com.openexchange.threadpool.prestartAllCoreThreads |
|:----------------|:--------|
| __Description__ | Starts all core threads, causing them to idly wait for work.<br>This overrides the default policy of starting core threads only when new tasks are executed. <br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Threadpool.html">Threadpool</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Thread.html">Thread</a> |
| __File__ | threadpool.properties |

---
| __Key__ | com.openexchange.threadpool.maximumPoolSize |
|:----------------|:--------|
| __Description__ | The maximum number of threads to allow in the pool.<br>The max. integer value of 2^31 - 1 is considered as unlimited max. number of threads.<br> |
| __Default__ | 2147483647 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Threadpool.html">Threadpool</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Thread.html">Thread</a> |
| __File__ | threadpool.properties |

---
| __Key__ | com.openexchange.threadpool.keepAliveTime |
|:----------------|:--------|
| __Description__ | When the number of threads is greater than the core, this is the maximum<br>time (in milliseconds) that excess idle threads will wait for new tasks before terminating.<br> |
| __Default__ | 60000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Threadpool.html">Threadpool</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Thread.html">Thread</a> |
| __File__ | threadpool.properties |

---
| __Key__ | com.openexchange.threadpool.workQueue |
|:----------------|:--------|
| __Description__ | The queue type to use for holding tasks before they are executed. This queue will<br>hold only the tasks submitted by the execute method.<br>Supported values: synchronous and linked<br>A synchronous queue is an appropriate choice when "com.openexchange.threadpool.maximumPoolSize"<br>is unlimited and possible rejection of tasks is allowed. A synchronous queue has no capacity,<br>it rather acts as a direct hand-off of tasks to an already waiting worker thread and will deny<br>the task if there is no further worker thread to process the task.<br>A linked queue is an appropriate choice when "com.openexchange.threadpool.maximumPoolSize"<br>is limited and rejection of tasks is prohibited. A linked queue has a (fixed) capacity to store<br>submitted tasks which have to wait for a worker thread to become ready.<br> |
| __Default__ | synchronous |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.threadpool.maximumPoolSize |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Threadpool.html">Threadpool</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Thread.html">Thread</a> |
| __File__ | threadpool.properties |

---
| __Key__ | com.openexchange.threadpool.workQueueSize |
|:----------------|:--------|
| __Description__ | The size of the work queue. Zero means unlimited size.<br>Note: If this property is set to a value greater than zero, property "com.openexchange.threadpool.workQueue"<br>is implicitly set to "linked" to accomplish a fixed-size work queue.<br> |
| __Default__ | 0 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.threadpool.workQueue |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Threadpool.html">Threadpool</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Thread.html">Thread</a> |
| __File__ | threadpool.properties |

---
| __Key__ | com.openexchange.threadpool.blocking |
|:----------------|:--------|
| __Description__ | Enable/disable blocking behavior. A blocking behavior means that caller is blocked until space becomes available in working queue.<br>This is useful for installation with limited capacities concerning max. number of threads and a bounded blocking work queue.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Threadpool.html">Threadpool</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Thread.html">Thread</a> |
| __File__ | threadpool.properties |

---
| __Key__ | com.openexchange.threadpool.refusedExecutionBehavior |
|:----------------|:--------|
| __Description__ | The default behavior to obey when execution is blocked because the thread bounds and queue<br>capacities are reached.<br>Supported values: abort, caller-runs, discard<br>- abort: Aborts execution by throwing an appropriate exception to the caller.<br>- caller-runs: The caller is considered to run the task if thread pool is unable to do so.<br>- discard: The task is silently discarded. No exception is thrown.<br> |
| __Default__ | abort |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Threadpool.html">Threadpool</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Thread.html">Thread</a> |
| __File__ | threadpool.properties |

---
| __Key__ | com.openexchange.quartz.localThreads |
|:----------------|:--------|
| __Description__ | Number of worker threads for the local scheduler instance.<br> |
| __Default__ | 5 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quartz.html">Quartz</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Thread.html">Thread</a> |
| __File__ | quartz.properties |

---
