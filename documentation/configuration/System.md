---
title: System
---

| __Key__ | com.openexchange.caching.configfile |
|:----------------|:--------|
| __Description__ | Location of default cache configuration file<br> |
| __Default__ | cache.ccf |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/System.html">System</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a> |
| __File__ | system.properties |

---
| __Key__ | serviceUsageInspection |
|:----------------|:--------|
| __Description__ | Enabled/disable service usage inspection. If enabled, all services<br>obtained and managed by ServiceHolder class will be tracked to ensure<br>all services are put back (via unget) within a certain amount of time.<br>The time range can be defined through property 'serviceUsageTimeout'.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/System.html">System</a> |
| __File__ | system.properties |

---
| __Key__ | serviceUsageTimeout |
|:----------------|:--------|
| __Description__ | The service usage timeout in milliseconds. This property only has<br>effect if property 'serviceUsageInspection' is set to 'true'.<br> |
| __Default__ | 10000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/System.html">System</a> |
| __File__ | system.properties |

---
| __Key__ | MimeTypeFileName |
|:----------------|:--------|
| __Description__ | Name of the MIME type file<br> |
| __Default__ | mime.types |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/System.html">System</a> |
| __File__ | system.properties |

---
| __Key__ | UserConfigurationStorage |
|:----------------|:--------|
| __Description__ | Name of the class implementing the UserConfigurationStorage.<br>"Currently known aliases:" Caching, DB<br> |
| __Default__ | Caching |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/System.html">System</a> |
| __File__ | system.properties |

---
| __Key__ | Cache |
|:----------------|:--------|
| __Description__ | Switch for enabling caching in the groupware. Normally this should be set to<br>true or the database will get a lot more load.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/System.html">System</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a> |
| __File__ | system.properties |

---
| __Key__ | CalendarSQL |
|:----------------|:--------|
| __Description__ | IMPORTEREXPORTER:@oxgroupwaresysconfdir@/importerExporter.xml<br> |
| __Default__ | com.openexchange.groupware.calendar.CalendarMySQL |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/System.html">System</a> |
| __File__ | system.properties |

---
| __Key__ | SERVER_NAME |
|:----------------|:--------|
| __Description__ | Server name registered by registerserver in the configuration database<br>can be read with listservers<br> |
| __Default__ | local |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/System.html">System</a> |
| __File__ | system.properties |

---
| __Key__ | com.openexchange.config.cascade.scopes |
|:----------------|:--------|
| __Description__ | The scopes to use in the config cascade, and their precedence<br> |
| __Default__ | user, context, contextSets, server |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/System.html">System</a> |
| __File__ | system.properties |

---
