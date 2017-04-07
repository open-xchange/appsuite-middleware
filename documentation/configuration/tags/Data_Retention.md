---
title: Data Retention
---

This page shows all properties with the tag: Data Retention

| __Key__ | com.openexchange.dataretention.dir |
|:----------------|:--------|
| __Description__ | The directory where the CSV file is held.<br> |
| __Default__ | /var/log/open-xchange |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Data_Retention.html">Data Retention</a> |
| __File__ | dataretention.properties |

---
| __Key__ | com.openexchange.dataretention.versionNumber |
|:----------------|:--------|
| __Description__ | The format version appended to each record type; e.g "H1" meaning "Header version 1".<br> |
| __Default__ | 1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Data_Retention.html">Data Retention</a> |
| __File__ | dataretention.properties |

---
| __Key__ | com.openexchange.dataretention.clientID |
|:----------------|:--------|
| __Description__ | The string identifying the tenant; e.g "Open-Xchange".<br> |
| __Default__ | Open-Xchange |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Data_Retention.html">Data Retention</a> |
| __File__ | dataretention.properties |

---
| __Key__ | com.openexchange.dataretention.sourceID |
|:----------------|:--------|
| __Description__ | The string identifying the data source; e.g. "OX_mail_01".<br> |
| __Default__ | OX_mail_01 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Data_Retention.html">Data Retention</a> |
| __File__ | dataretention.properties |

---
| __Key__ | com.openexchange.dataretention.location |
|:----------------|:--------|
| __Description__ | The location of the system generating the retention data<br> |
| __Default__ | DE/Olpe |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Data_Retention.html">Data Retention</a> |
| __File__ | dataretention.properties |

---
| __Key__ | com.openexchange.dataretention.timeZone |
|:----------------|:--------|
| __Description__ | The time zone of the location.<br> |
| __Default__ | GMT |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Data_Retention.html">Data Retention</a> |
| __File__ | dataretention.properties |

---
| __Key__ | com.openexchange.dataretention.rotateLength |
|:----------------|:--------|
| __Description__ | Specifies the max. output resource's length (in bytes) before it gets rotated.<br>This option is only useful for implementations which output data to a file or<br>to any limited resource. This value should have a reasonable size since multiple<br>write accesses may occur at same time. Therefore small sizes (<= 200KB) cannot<br>be guaranteed being obeyed.<br>Moreover it is only an approximate limit which can vary about 8KB.<br>A value less than or equal to zero means no rotation.<br> |
| __Default__ | 0 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Data_Retention.html">Data Retention</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | dataretention.properties |

---
