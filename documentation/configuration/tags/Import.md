---
title: Import
---

This page shows all properties with the tag: Import

| __Key__ | com.openexchange.import.mapper.path |
|:----------------|:--------|
| __Description__ | Configures path to mappings of ox fields to other csv formats (like Outlook).<br> |
| __Default__ | @rootinstalldir@/importCSV/ |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Import.html">Import</a> |
| __File__ | import.properties.in |

---
| __Key__ | com.openexchange.import.ical.limit |
|:----------------|:--------|
| __Description__ | Sets a limit on how many entries a single import of ical data may contain.<br>Note that this limit applies for each type, so you can have, for example, 10000 VEVENTS and 10000 VFREEBUSY entries in a single file. <br>-1 means unlimited.<br> |
| __Default__ | 10000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Import.html">Import</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | import.properties.in |

---
| __Key__ | com.openexchange.import.contacts.limit |
|:----------------|:--------|
| __Description__ | Sets the limit on how many contacts can be imported at once.<br>-1 means unlimited.<br> |
| __Default__ | -1 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Import.html">Import</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | import.properties.in |

---
