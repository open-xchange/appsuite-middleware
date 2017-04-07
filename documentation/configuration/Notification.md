---
title: Notification
---

The settings on this page govern, when notification eMails are sent once a task or appointment is modified.
Whether notification mails are sent is goverened 
a) by a user specific setting in the configuration. 
b) by the checkbox when creating a task/appointment.
c) by this settings.

a) The user specific setting
Each user can specify whether he wants to receive notification emails for tasks or appointments. The user can
set this individually per object type. If the user chooses not to be notified for (say) tasks, that's the end of
the story, the notification mails won't ever bother her (for tasks). She may choose separately that she's interested
about appointment notifications, and enable the corresponding feature. This would mean she might be notified of changes
to appointments in which she is a participant. This is goverened by the other settings.

b) The checkbox when creating/modifying a task/appointment
Notice in the dialog when creating or editing an appointment there is a checkbox labeled 'Notify Participants'.
If this checkbox is enabled, the participants of the task/appointment will be notified of the change (unless their
user specific setting as per a) suppress that). Also the person creating or changing the object will not get the
notification mail, because, presumably, she knows what she has been doing. Also the owner is always notified
of a change to a task/appointment by someone else other than the owner (unless the notification is suppressed as per a) ).

c) Settings on this page
Sometimes an action doesn't allow you to check the checkbox under b) but it might still be a good idea to notify
participants of an action. For example, when deleting an appointment this is usually of great interest to many people.
So the setting 'notify_participants_on_delete' in this file supersedes the checkbox setting. If set to 'true' all
participants will be notified when the appointment or task is deleted with the exception of the person deleting
the appointment/task.


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
