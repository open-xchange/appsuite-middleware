---
title: Quota
---

This page shows all properties with the tag: Quota

| __Key__ | com.openexchange.quota.share_links |
|:----------------|:--------|
| __Description__ | Specifies the quota for the number of share links that are allowed being <br>created by one user. A value < 0 means unlimited.<br> |
| __Default__ | 100 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a> |
| __File__ | share.properties |

---
| __Key__ | com.openexchange.quota.invite_guests |
|:----------------|:--------|
| __Description__ | Specifies the quota for the number of guest users that are allowed being <br>created by one user. A value < 0 means unlimited.<br> |
| __Default__ | 100 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a> |
| __File__ | share.properties |

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
| __Key__ | com.openexchange.preview.cache.quota |
|:----------------|:--------|
| __Description__ | Specify the total quota for preview cache for each context<br>This value is used if no individual context quota is defined.<br>A value of zero or less means no quota<br> |
| __Default__ | 10485760 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Preview.html">Preview</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a> |
| __File__ | preview.properties |

---
| __Key__ | com.openexchange.preview.cache.quotaPerDocument |
|:----------------|:--------|
| __Description__ | Specify the quota per document for preview cache for each context<br>This value is used if no individual quota per document is defined.<br>A value of zero or less means no quota<br> |
| __Default__ | 524288 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Preview.html">Preview</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a> |
| __File__ | preview.properties |

---
| __Key__ | com.openexchange.preview.cache.quotaAware |
|:----------------|:--------|
| __Description__ | Specifies if storing previews in file store affects user's file store quota or not<br>Only applies if "com.openexchange.preview.cache.type" is set to "FS"<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Preview.html">Preview</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a> |
| __File__ | preview.properties |

---
