---
title: Report client
---

The first part defines if a proxy server should be used to establish the http connection
to the Open-Xchange report server, and if which server and proxy details to use.
The second part configures report serialization.


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
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Report.html">Report</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Proxy.html">Proxy</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
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
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Report.html">Report</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Proxy.html">Proxy</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | reportclient.properties |

---
| __Key__ | com.openexchange.report.client.proxy.password |
|:----------------|:--------|
| __Description__ | The password that should be used to autherise on the proxy server.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.report.client.proxy.username |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Report.html">Report</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Proxy.html">Proxy</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
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
