---
title: Task
---

This page shows all properties with the tag: Task

| __Key__ | notify_participants_on_delete |
|:----------------|:--------|
| __Description__ | If set to 'true' all participants will be notified when the appointment or task is deleted <br>with the exception of the person deleting the appointment/task.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Notification.html">Notification</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Task.html">Task</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | notification.properties |

---
| __Key__ | object_link |
|:----------------|:--------|
| __Description__ | Direct links for notifications are generated according to the following pattern.<br>[hostname] needs to be replaced with the hostname of your machine. This is done automatically by software on backend machines using the<br>hosts canonical host name.<br>[uiwebpath] is replaced with the value of com.openexchange.UIWebPath defined in server.properties.<br>[module], [object] and [folder] are replaced with the relevant IDs to generate the direct link.<br> |
| __Default__ | http://[hostname]/[uiwebpath]#!!&app=io.ox/[module]&id=[object]&folder=[folder] |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Notification.html">Notification</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Task.html">Task</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a> |
| __File__ | notification.properties |

---
| __Key__ | imipForInternalUsers |
|:----------------|:--------|
| __Description__ | Enables/Disables imip-mails for internal users.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Notification.html">Notification</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Task.html">Task</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | notification.properties |

---
| __Key__ | com.openexchange.notification.fromSource |
|:----------------|:--------|
| __Description__ | This property defines which email address of a user is used as from header when this user triggers notification mails.<br>Possible values are: "primaryMail" or "defaultSenderAddress".<br> |
| __Default__ | defaultSenderAddress |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Notification.html">Notification</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Task.html">Task</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | notification.properties |

---
| __Key__ | com.openexchange.calendar.notify.poolenabled |
|:----------------|:--------|
| __Description__ | This property enables/disables the notification Pool for Appointment notifications and invitations.<br>If enabled, changes are combined and mails are summarized according different heuristics.<br>If disabled, changes are sent immediately.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Notification.html">Notification</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Task.html">Task</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | notification.properties |

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
