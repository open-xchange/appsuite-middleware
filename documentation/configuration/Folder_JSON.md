---
title: Folder JSON
---

| __Key__ | com.openexchange.folder.json.module |
|:----------------|:--------|
| __Description__ | Define the module which also serves as the appendix for registered servlet:<br>If module is set to "myfolders", the servlet path is changed to "/ajax/myfolders"<br>Default is "folders"<br> |
| __Default__ | folders |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/JSON.html">JSON</a> |
| __File__ | folderjson.properties |

---
| __Key__ | com.openexchange.folder.json.servletPath |
|:----------------|:--------|
| __Description__ | Define the path for registered servlet<br>If set, the complete path is set to specified value regardless of the value of "com.openexchange.folder.json.module" property<br>Default is empty to let path be: "/ajax/" + <module><br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/JSON.html">JSON</a> |
| __File__ | folderjson.properties |

---
