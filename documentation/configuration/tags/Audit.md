---
title: Audit
---

This page shows all properties with the tag: Audit

| __Key__ | com.openexchange.audit.logging.AuditFileHandler.enabled |
|:----------------|:--------|
| __Description__ | Enables the audit feature.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Audit.html">Audit</a> |
| __File__ | audit.properties |

---
| __Key__ | com.openexchange.audit.logging.AuditFileHandler.pattern |
|:----------------|:--------|
| __Description__ | The location of the log file.<br> |
| __Default__ | /var/log/open-xchange/audit.log |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Audit.html">Audit</a> |
| __File__ | audit.properties |

---
| __Key__ | com.openexchange.audit.logging.AuditFileHandler.level |
|:----------------|:--------|
| __Description__ | The log level.<br> |
| __Default__ | ALL |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Audit.html">Audit</a> |
| __File__ | audit.properties |

---
| __Key__ | com.openexchange.audit.logging.AuditFileHandler.limit |
|:----------------|:--------|
| __Description__ | The maximum file size.<br> |
| __Default__ | 2097152 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Audit.html">Audit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | audit.properties |

---
| __Key__ | com.openexchange.audit.logging.AuditFileHandler.formatter |
|:----------------|:--------|
| __Description__ | The logging formatter.<br> |
| __Default__ | java.util.logging.SimpleFormatter |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Audit.html">Audit</a> |
| __File__ | audit.properties |

---
| __Key__ | com.openexchange.audit.logging.AuditFileHandler.append |
|:----------------|:--------|
| __Description__ | A flag indicating whether the log should be appended or not.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Audit.html">Audit</a> |
| __File__ | audit.properties |

---
| __Key__ | com.openexchange.audit.logging.FileAccessLogging.enabled |
|:----------------|:--------|
| __Description__ | Set to true to enable logging of file accesses and downloads.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Audit.html">Audit</a> |
| __File__ | audit.properties |

---
| __Key__ | com.openexchange.log.audit.slf4j.enabled |
|:----------------|:--------|
| __Description__ | Enables/disables audit logging.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Audit.html">Audit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Logging.html">Logging</a> |
| __File__ | slf4j-auditlog.properties |

---
| __Key__ | com.openexchange.log.audit.slf4j.level |
|:----------------|:--------|
| __Description__ | Specifies the log level to use.<br>Possible values: "trace", "debug", "info", "warn" or "error".<br> |
| __Default__ | info |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Audit.html">Audit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Logging.html">Logging</a> |
| __File__ | slf4j-auditlog.properties |

---
| __Key__ | com.openexchange.log.audit.slf4j.delimiter |
|:----------------|:--------|
| __Description__ | Specifies the delimiter to use. Surrounding quotes are removed for real usage.<br> |
| __Default__ | ", " |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Audit.html">Audit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Logging.html">Logging</a> |
| __File__ | slf4j-auditlog.properties |

---
| __Key__ | com.openexchange.log.audit.slf4j.includeAttributeNames |
|:----------------|:--------|
| __Description__ | Specifies whether attribute names shall be logged.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Audit.html">Audit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Logging.html">Logging</a> |
| __File__ | slf4j-auditlog.properties |

---
| __Key__ | com.openexchange.log.audit.slf4j.date.pattern |
|:----------------|:--------|
| __Description__ | Specifies the optional date pattern to use.<br>Accepts a pattern according to: https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html. <br><br>By default "com.openexchange.log.audit.slf4j.date.pattern" is empty, which means standard ISO-8601 formatting is used and accompanying properties<br>"com.openexchange.log.audit.slf4j.date.locale" and "com.openexchange.log.audit.slf4j.date.timezone" are ignored.<br><br>If a pattern is specified for "com.openexchange.log.audit.slf4j.date.pattern" the accompanying properties may optionally be used to also define<br>the locale and time zone to use for date formatting.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.log.audit.slf4j.date.locale, com.openexchange.log.audit.slf4j.date.timezone |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Audit.html">Audit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Logging.html">Logging</a> |
| __File__ | slf4j-auditlog.properties |

---
| __Key__ | com.openexchange.log.audit.slf4j.date.locale |
|:----------------|:--------|
| __Description__ | Specifies the locale to use for date formatting.<br> |
| __Default__ | en_US |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.log.audit.slf4j.date.pattern |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Audit.html">Audit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Logging.html">Logging</a> |
| __File__ | slf4j-auditlog.properties |

---
| __Key__ | com.openexchange.log.audit.slf4j.date.timezone |
|:----------------|:--------|
| __Description__ | Specifies the timezone to use for date formatting.<br> |
| __Default__ | GMT |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.log.audit.slf4j.date.pattern |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Audit.html">Audit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Logging.html">Logging</a> |
| __File__ | slf4j-auditlog.properties |

---
| __Key__ | com.openexchange.log.audit.slf4j.file.location |
|:----------------|:--------|
| __Description__ | Specifies the file location to use.<br><br>By default "com.openexchange.log.audit.slf4j.file.location" is empty, which means regular App Suite logging is used and accompanying properties<br>"com.openexchange.log.audit.slf4j.file.size", "com.openexchange.log.audit.slf4j.file.count" and "com.openexchange.log.audit.slf4j.file.pattern" are ignored.<br><br>Once a file location/pattern is set, audit logger will no more use regular App Suite logging, but output its log messages to rotating files.<br>E.g. a file location/pattern might be: "/var/log/open-xchange/my-audit.log"<br>It is then possible to configure how that logging is done:<br> - "com.openexchange.log.audit.slf4j.file.size" specifies the max. file size to use<br> - "com.openexchange.log.audit.slf4j.file.count" defines the max. number of files to use for rotation<br> - "com.openexchange.log.audit.slf4j.file.pattern" defines the layout pattern of the log entry; <br>    for more patterns see http://logback.qos.ch/manual/layouts.html#ClassicPatternLayout<br> |
| __Default__ | GMT |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.log.audit.slf4j.file.size, com.openexchange.log.audit.slf4j.file.count, com.openexchange.log.audit.slf4j.file.pattern |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Audit.html">Audit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Logging.html">Logging</a> |
| __File__ | slf4j-auditlog.properties |

---
| __Key__ | com.openexchange.log.audit.slf4j.file.size |
|:----------------|:--------|
| __Description__ | Specifies the max. file size to use.<br> |
| __Default__ | 2097152 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.log.audit.slf4j.file.location |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Audit.html">Audit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Logging.html">Logging</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | slf4j-auditlog.properties |

---
| __Key__ | com.openexchange.log.audit.slf4j.file.count |
|:----------------|:--------|
| __Description__ | Specifies the max. number of files to use for rotation.<br> |
| __Default__ | 99 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.log.audit.slf4j.file.location |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Audit.html">Audit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Logging.html">Logging</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | slf4j-auditlog.properties |

---
| __Key__ | com.openexchange.log.audit.slf4j.file.pattern |
|:----------------|:--------|
| __Description__ | Defines the layout pattern of the log entry; <br>for more patterns see http://logback.qos.ch/manual/layouts.html#ClassicPatternLayout<br> |
| __Default__ | "%sanitisedMessage%n" |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.log.audit.slf4j.file.location |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Audit.html">Audit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Logging.html">Logging</a> |
| __File__ | slf4j-auditlog.properties |

---
