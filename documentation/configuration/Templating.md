---
title: Templating
---

| __Key__ | com.openexchange.templating.path |
|:----------------|:--------|
| __Description__ | Specifies the templating path.<br> |
| __Default__ | @templatepath@ |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Template.html">Template</a> |
| __File__ | templating.properties |

---
| __Key__ | com.openexchange.templating.assets.path |
|:----------------|:--------|
| __Description__ | Specifies the assets path.<br> |
| __Default__ | @templatepath@/assets/ |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Template.html">Template</a> |
| __File__ | templating.properties |

---
| __Key__ | com.openexchange.templating.trusted |
|:----------------|:--------|
| __Description__ | Define which kind of templates should be trusted.<br>Possible values are "server", "user", "true" and "false".<br> |
| __Default__ | server |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Template.html">Template</a> |
| __File__ | templating.properties |

---
| __Key__ | com.openexchange.templating.usertemplating |
|:----------------|:--------|
| __Description__ | Define whether users can use templating. This would involve <br>applying templates located in the user's OXMF Templates folder.<br>If set to false, only server templates will be used.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Template.html">Template</a> |
| __File__ | templating.properties |

---
