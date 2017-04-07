---
title: Quota
---

| __Key__ | com.openexchange.quota.calendar |
|:----------------|:--------|
| __Description__ | Specifies the quota for the number of appointments that are allowed being created within a single context (tenant-wise scope).<br><br>The purpose of this quota is to define a rough upper limit that is unlikely being reached during normal operation.<br>Therefore it is rather supposed to prevent from excessive item creation (e.g. a synchronizing client running mad),<br>but not intended to have a fine-grained quota setting. Thus exceeding that quota limitation will cause an appropriate<br>exception being thrown, denying to further create any appointment in affected context.<br> |
| __Default__ | 250000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | quota.properties |

---
| __Key__ | com.openexchange.quota.task |
|:----------------|:--------|
| __Description__ | Specifies the quota for the number of tasks that are allowed being created within a single context (tenant-wise scope).<br><br>The purpose of this quota is to define a rough upper limit that is unlikely being reached during normal operation.<br>Therefore it is rather supposed to prevent from excessive item creation (e.g. a synchronizing client running mad),<br>but not intended to have a fine-grained quota setting. Thus exceeding that quota limitation will cause an appropriate<br>exception being thrown, denying to further create any task in affected context.<br> |
| __Default__ | 250000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Task.html">Task</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | quota.properties |

---
| __Key__ | com.openexchange.quota.contact |
|:----------------|:--------|
| __Description__ | Specifies the quota for the number of contacts that are allowed being created within a single context (tenant-wise scope).<br><br>The purpose of this quota is to define a rough upper limit that is unlikely being reached during normal operation.<br>Therefore it is rather supposed to prevent from excessive item creation (e.g. a synchronizing client running mad),<br>but not intended to have a fine-grained quota setting. Thus exceeding that quota limitation will cause an appropriate<br>exception being thrown, denying to further create any contact in affected context.<br> |
| __Default__ | 250000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | quota.properties |

---
| __Key__ | com.openexchange.quota.infostore |
|:----------------|:--------|
| __Description__ | Specifies the quota for the number of documents that are allowed being created within a single context (tenant-wise scope).<br><br>The purpose of this quota is to define a rough upper limit that is unlikely being reached during normal operation.<br>Therefore it is rather supposed to prevent from excessive item creation (e.g. a synchronizing client running mad),<br>but not intended to have a fine-grained quota setting. Thus exceeding that quota limitation will cause an appropriate<br>exception being thrown, denying to further create any document in affected context.<br> |
| __Default__ | 250000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Infostore.html">Infostore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | quota.properties |

---
| __Key__ | com.openexchange.quota.attachment |
|:----------------|:--------|
| __Description__ | Specifies the quota for the number of attachments bound to PIM objects that are allowed being created within a single context (tenant-wise scope).<br><br>The purpose of this quota is to define a rough upper limit that is unlikely being reached during normal operation.<br>Therefore it is rather supposed to prevent from excessive item creation (e.g. a synchronizing client running mad),<br>but not intended to have a fine-grained quota setting. Thus exceeding that quota limitation will cause an appropriate<br>exception being thrown, denying to further create any attachment in affected context.<br> |
| __Default__ | 250000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Attachment.html">Attachment</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | quota.properties |

---
