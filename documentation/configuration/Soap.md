---
title: Soap
---

| __Key__ | com.openexchange.soap.cxf.baseAddress |
|:----------------|:--------|
| __Description__ | Specifies the base address for published end points; e.g. "http://www.myserver.com/myservices"<br>An empty value means that the running machine's address is used.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Soap.html">Soap</a> |
| __File__ | soap-cxf.properties |

---
| __Key__ | com.openexchange.soap.cxf.hideServiceListPage |
|:----------------|:--------|
| __Description__ | Specifies whether to hice service list page.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Soap.html">Soap</a> |
| __File__ | soap-cxf.properties |

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
