---
title: Report
---

This page shows all properties with the tag: Report

| __Key__ | com.openexchange.report.appsuite.fileStorage |
|:----------------|:--------|
| __Description__ | Reports filestorage directory for storage of report parts and composed data.<br> |
| __Default__ | /tmp |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Report.html">Report</a> |
| __File__ | reportserialization.properties |

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
| __Key__ | com.openexchange.report.appsuite.threadPriority |
|:----------------|:--------|
| __Description__ | The priority that threads, working on the report have. Allowed value range is 1-10. 1 is the lowest, 10 the highest priority.<br> |
| __Default__ | 1 |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Report.html">Report</a> |
| __File__ | reportserialization.properties |

---
| __Key__ | com.openexchange.report.client.proxy.useproxy |
|:----------------|:--------|
| __Description__ | Determine wether a proxy should be used or not. If set to true, the related properties have to be set also.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.report.client.proxy.address, com.openexchange.report.client.proxy.port, com.openexchange.report.client.proxy.authrequired |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Report.html">Report</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Proxy.html">Proxy</a> |
| __File__ | reportclient.properties |

---
| __Key__ | com.openexchange.report.client.proxy.address |
|:----------------|:--------|
| __Description__ | The address of the proxy, that should be used.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.report.client.proxy.port |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Report.html">Report</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Proxy.html">Proxy</a> |
| __File__ | reportclient.properties |

---
| __Key__ | com.openexchange.report.client.proxy.port |
|:----------------|:--------|
| __Description__ | The port of the proxy, that should be used.<br> |
| __Default__ | 8080 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.report.client.proxy.address |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Report.html">Report</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Proxy.html">Proxy</a> |
| __File__ | reportclient.properties |

---
| __Key__ | com.openexchange.report.client.proxy.authrequired |
|:----------------|:--------|
| __Description__ | Is authorisation required to use the proxy. If set to true, the related properties have to be set also.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.report.client.proxy.username, com.openexchange.report.client.proxy.password |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Report.html">Report</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Proxy.html">Proxy</a> |
| __File__ | reportclient.properties |

---
| __Key__ | com.openexchange.report.client.proxy.username |
|:----------------|:--------|
| __Description__ | The username that should be used to autherise on the proxy server.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.report.client.proxy.password |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Report.html">Report</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Proxy.html">Proxy</a> |
| __File__ | reportclient.properties |

---
| __Key__ | com.openexchange.report.client.proxy.password |
|:----------------|:--------|
| __Description__ | The password that should be used to autherise on the proxy server.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.report.client.proxy.username |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Report.html">Report</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Proxy.html">Proxy</a> |
| __File__ | reportclient.properties |

---
| __Key__ | com.openexchange.report.appsuite.fileStorage |
|:----------------|:--------|
| __Description__ | Where should parts of the report be stored.<br> |
| __Default__ | /tmp |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Report.html">Report</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Serialization.html">Serialization</a> |
| __File__ | reportclient.properties |

---
| __Key__ | com.openexchange.report.appsuite.maxChunkSize |
|:----------------|:--------|
| __Description__ | How many capability-Sets should be hold in memory before writing them into a file.<br> |
| __Default__ | 200 |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Report.html">Report</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Serialization.html">Serialization</a> |
| __File__ | reportclient.properties |

---
| __Key__ | com.openexchange.report.appsuite.maxThreadPoolSize |
|:----------------|:--------|
| __Description__ | How many threads can be used for report processing.<br> |
| __Default__ | 20 |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Report.html">Report</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Serialization.html">Serialization</a> |
| __File__ | reportclient.properties |

---
| __Key__ | com.openexchange.report.appsuite.threadPriority |
|:----------------|:--------|
| __Description__ | Which thread priority do the processing threads have.<br> |
| __Default__ | 1 |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Report.html">Report</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Serialization.html">Serialization</a> |
| __File__ | reportclient.properties |

---
