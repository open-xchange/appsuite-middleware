---
title: Publication
---

This page shows all properties with the tag: Publication

| __Key__ | com.openexchange.publish.microformats.usesWhitelisting |
|:----------------|:--------|
| __Description__ | If set to true, the contents of a microformatted publication will<br>be processed through a whitelisting filter that removes elements<br>that are considered potentially unsafe. We recommend you use this<br>in case you do not have publications on a different subdomain.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Publication.html">Publication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/White_List.html">White List</a> |
| __File__ | publications.properties |

---
| __Key__ | com.openexchange.publish.domain |
|:----------------|:--------|
| __Description__ | If enabled, this allows you to have a different domain as<br>part of the URI for publications.<br>NOTE: If you enable both the domain and the subdomain option,<br>domain takes precedence.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.publish.subdomain |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Publication.html">Publication</a> |
| __File__ | publications.properties |

---
| __Key__ | com.openexchange.publish.subdomain |
|:----------------|:--------|
| __Description__ | If enabled, this allows you to use a different subdomain as<br>part of the URI for publications. This subdomain will be<br>prepended to the domain of the server you are currently using.<br>No need to end the subdomain name with a dot, btw.<br>NOTE: If you enable both the domain and the subdomain option,<br>domain takes precedence.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.publish.domain |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Publication.html">Publication</a> |
| __File__ | publications.properties |

---
| __Key__ | com.openexchange.publish.legalHosterName |
|:----------------|:--------|
| __Description__ | The legal name of the company that will be displayed in the<br>disclaimer of the default publication template.<br>NOTE: If no value is set or the property is commented out, the<br>default will be the generic wording 'the hoster'.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Publication.html">Publication</a> |
| __File__ | publications.properties |

---
| __Key__ | com.openexchange.publish.createModifyEnabled |
|:----------------|:--------|
| __Description__ | Enabled/disables the possibility to create/modify publications<br>Default is false (as new sharing module is supposed to be used and this module is considered as deprecated)<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Publication.html">Publication</a> |
| __File__ | publications.properties |

---
