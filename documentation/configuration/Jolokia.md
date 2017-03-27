---
title: Jolokia
---

This page describes the properties for jolokia contained in the com.openexchange.jolokia bundle.


| __Key__ | com.openexchange.jolokia.start |
|:----------------|:--------|
| __Description__ | Whether to start jolokia or not.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Jolokia.html">Jolokia</a> |
| __File__ | jolokia.properties |

---
| __Key__ | com.openexchange.jolokia.servlet.name |
|:----------------|:--------|
| __Description__ | Under what servlet name jolokia will be published, please bear in mind that this should not be forwarded by apache and kept internal.<br> |
| __Default__ | /monitoring/jolokia |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Jolokia.html">Jolokia</a> |
| __File__ | jolokia.properties |

---
| __Key__ | com.openexchange.jolokia.user |
|:----------------|:--------|
| __Description__ | User used for authentication with HTTP Basic Authentication. If not set, jolokia won't start.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Jolokia.html">Jolokia</a> |
| __File__ | jolokia.properties |

---
| __Key__ | com.openexchange.jolokia.password |
|:----------------|:--------|
| __Description__ | Password used for authentification with HTTP Basic Authentication. If not set, jolokia won't start.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Jolokia.html">Jolokia</a> |
| __File__ | jolokia.properties |

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
| __Key__ | com.openexchange.jolokia.restrict.to.localhost |
|:----------------|:--------|
| __Description__ | This setting will restrict jolokia access to localhost. It is completly ignored when a jolokia-access.xml is present.<br>It is also a second guard and bound to com.openexchange.connector.networkListenerHost inside server.properties<br>As Jolokia uses the http interface, it is bound to the host for the connector's http network listener<br>which is configured by com.openexchange.connector.networkListenerHost.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.connector.networkListenerHost |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Jolokia.html">Jolokia</a> |
| __File__ | jolokia.properties |

---
