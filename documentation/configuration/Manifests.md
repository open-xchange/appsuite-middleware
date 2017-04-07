---
title: Manifests
---

Frontend properties of OX App Suite.
These properties configure the installation-specific settings of the web front-end.


| __Key__ | com.openexchange.apps.path |
|:----------------|:--------|
| __Description__ | Path to the installation directory of UI apps.<br> |
| __Default__ | @prefix@/appsuite |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Manifests.html">Manifests</a> |
| __File__ | manifests.properties.in |

---
| __Key__ | com.openexchange.apps.tzdata |
|:----------------|:--------|
| __Description__ | Path to the zoneinfo database.<br> |
| __Default__ | /usr/share/zoneinfo |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Manifests.html">Manifests</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | manifests.properties.in |

---
| __Key__ | com.openexchange.apps.manifestPath |
|:----------------|:--------|
| __Description__ | Paths to directories with UI manifest files.<br> |
| __Default__ | @prefix@/appsuite/manifests:<more paths> |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Manifests.html">Manifests</a> |
| __File__ | manifests.properties.in |

---
