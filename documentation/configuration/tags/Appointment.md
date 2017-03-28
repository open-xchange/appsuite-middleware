---
title: Appointment
---

This page shows all properties with the tag: Appointment

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
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Notification.html">Notification</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Task.html">Task</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
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
| __Key__ | com.openexchange.share.handler.iCal.futureInterval |
|:----------------|:--------|
| __Description__ | Defines up to which time in the future appointments and tasks are included <br>when accessing an iCal-serializable share with a client requesting the <br>"text/calendar" representation of the resource.<br>Possible values are "one_month", "six_months", "one_year" and "two_years".<br> |
| __Default__ | one_year |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Ical.html">Ical</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a> |
| __File__ | share.properties |

---
| __Key__ | com.openexchange.share.handler.iCal.pastInterval |
|:----------------|:--------|
| __Description__ | Defines up to which time in the past appointments and tasks are included <br>when accessing an iCal-serializable share with a client requesting the <br>"text/calendar" representation of the resource.<br>Possible values are "two_weeks", "one_month", "six_months" and "one_year".<br> |
| __Default__ | two_weeks |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Ical.html">Ical</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a> |
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
| __Key__ | MAX_UPLOAD_SIZE |
|:----------------|:--------|
| __Description__ | If the sum of all uploaded files (for contacts, appointments or tasks) in one request is larger than this value,<br>the upload will be rejected. If this value is not set or -1, the more general MAX_UPLOAD_SIZE configured in<br>server.properties will be used. If that value is 0 uploads will be unrestricted.<br>The size is in Bytes.<br> |
| __Default__ | 10485760 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Attachment.html">Attachment</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Tasks.html">Tasks</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | attachment.properties |

---
| __Key__ | com.openexchange.calendar.seriesconflictlimit |
|:----------------|:--------|
| __Description__ | This boolean option switches on/off the limitation for the<br>conflict search for a series to 1 year in the future. This<br>means, that a new/changed series will not conflict with<br>appointments which are later than one year after the<br>creation/change of the appointment.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Calendar.html">Calendar</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a> |
| __File__ | calendar.properties |

---
| __Key__ | com.openexchange.caldav.interval.start |
|:----------------|:--------|
| __Description__ | Appointments and tasks are available via the CalDAV interface if they fall <br>into a configurable timeframe. This value specifies the start time of this <br>interval, i.e. how far past appointments should be considered. More formal, <br>this value defines the negative offset relative to the current date <br>representing the minimum end time of appointments to be synchronized.<br>Possible values are "one_month", "one_year" and "six_months". <br> |
| __Default__ | one_month |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CalDAV.html">CalDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a> |
| __File__ | caldav.properties |

---
| __Key__ | com.openexchange.caldav.interval.end |
|:----------------|:--------|
| __Description__ | Appointments and tasks are available via the CalDAV interface if they fall <br>into a configurable timeframe. This value specifies the end time of this<br>interval, i.e. how far future appointments should be considered. More <br>formal, this value defines the positive offset relative to the current date <br>representing the maximum start time of appointments to be synchronized.<br>Possible values are "one_year" and "two_years".<br> |
| __Default__ | one_year |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CalDAV.html">CalDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a> |
| __File__ | caldav.properties |

---
