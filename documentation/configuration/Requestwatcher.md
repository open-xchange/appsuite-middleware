---
title: Requestwatcher
---

| __Key__ | com.openexchange.requestwatcher.isEnabled |
|:----------------|:--------|
| __Description__ | Enable/disable the requestwatcher<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Requestwatcher.html">Requestwatcher</a> |
| __File__ | requestwatcher.properties |

---
| __Key__ | com.openexchange.requestwatcher.frequency |
|:----------------|:--------|
| __Description__ | Define the requestwatcher's frequency in milliseconds<br> |
| __Default__ | 30000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Requestwatcher.html">Requestwatcher</a> |
| __File__ | requestwatcher.properties |

---
| __Key__ | com.openexchange.requestwatcher.maxRequestAge |
|:----------------|:--------|
| __Description__ | Define the requestwatcher's frequency in milliseconds<br> |
| __Default__ | 60000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Requestwatcher.html">Requestwatcher</a> |
| __File__ | requestwatcher.properties |

---
| __Key__ | com.openexchange.requestwatcher.restartPermission |
|:----------------|:--------|
| __Description__ | Permission to stop & re-init system (works only for the ajp connector)<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Requestwatcher.html">Requestwatcher</a> |
| __File__ | requestwatcher.properties |

---
| __Key__ | com.openexchange.requestwatcher.eas.ignore.cmd |
|:----------------|:--------|
| __Description__ | Define a comma separated list of EAS commands that will be ignored by the<br>request watcher.<br> |
| __Default__ | sync,ping |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Requestwatcher.html">Requestwatcher</a> |
| __File__ | requestwatcher.properties |

---
| __Key__ | com.openexchange.requestwatcher.usm.ignore.path |
|:----------------|:--------|
| __Description__ | "Define a comma separated list of USM paths that will be ignored by the<br>request watcher. Hint: each path has to start with a '/'"<br> |
| __Default__ | /syncUpdate |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Requestwatcher.html">Requestwatcher</a> |
| __File__ | requestwatcher.properties |

---
