---
title: Threadpool
---

This page shows all properties with the tag: Threadpool

| __Key__ | com.openexchange.threadpool.corePoolSize |
|:----------------|:--------|
| __Description__ | The number of threads to keep in the pool, even if they are idle.<br>If unsure follow this rule: Number of CPUs + 1.<br> |
| __Default__ | 3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Threadpool.html">Threadpool</a> |
| __File__ | threadpool.properties |

---
| __Key__ | com.openexchange.threadpool.prestartAllCoreThreads |
|:----------------|:--------|
| __Description__ | Starts all core threads, causing them to idly wait for work.<br>This overrides the default policy of starting core threads only when new tasks are executed. <br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Threadpool.html">Threadpool</a> |
| __File__ | threadpool.properties |

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
| __Key__ | com.openexchange.threadpool.workQueue |
|:----------------|:--------|
| __Description__ | The queue type to use for holding tasks before they are executed. This queue will<br>hold only the tasks submitted by the execute method.<br>Supported values: synchronous and linked<br>A synchronous queue is an appropriate choice when "com.openexchange.threadpool.maximumPoolSize"<br>is unlimited and possible rejection of tasks is allowed. A synchronous queue has no capacity,<br>it rather acts as a direct hand-off of tasks to an already waiting worker thread and will deny<br>the task if there is no further worker thread to process the task.<br>A linked queue is an appropriate choice when "com.openexchange.threadpool.maximumPoolSize"<br>is limited and rejection of tasks is prohibited. A linked queue has a (fixed) capacity to store<br>submitted tasks which have to wait for a worker thread to become ready.<br> |
| __Default__ | synchronous |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.threadpool.maximumPoolSize |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Threadpool.html">Threadpool</a> |
| __File__ | threadpool.properties |

---
| __Key__ | com.openexchange.threadpool.workQueueSize |
|:----------------|:--------|
| __Description__ | The size of the work queue. Zero means unlimited size.<br>Note: If this property is set to a value greater than zero, property "com.openexchange.threadpool.workQueue"<br>is implicitly set to "linked" to accomplish a fixed-size work queue.<br> |
| __Default__ | 0 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.threadpool.workQueue |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Threadpool.html">Threadpool</a> |
| __File__ | threadpool.properties |

---
| __Key__ | com.openexchange.threadpool.blocking |
|:----------------|:--------|
| __Description__ | Enable/disable blocking behavior. A blocking behavior means that caller is blocked until space becomes available in working queue.<br>This is useful for installation with limited capacities concerning max. number of threads and a bounded blocking work queue.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Threadpool.html">Threadpool</a> |
| __File__ | threadpool.properties |

---
| __Key__ | com.openexchange.threadpool.refusedExecutionBehavior |
|:----------------|:--------|
| __Description__ | The default behavior to obey when execution is blocked because the thread bounds and queue<br>capacities are reached.<br>Supported values: abort, caller-runs, discard<br>- abort: Aborts execution by throwing an appropriate exception to the caller.<br>- caller-runs: The caller is considered to run the task if thread pool is unable to do so.<br>- discard: The task is silently discarded. No exception is thrown.<br> |
| __Default__ | abort |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Threadpool.html">Threadpool</a> |
| __File__ | threadpool.properties |

---
