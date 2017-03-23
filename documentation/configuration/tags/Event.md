---
title: Event
---

This page shows all properties with the tag: Event

| __Key__ | CHECK_AND_REMOVE_PAST_REMINDERS |
|:----------------|:--------|
| __Description__ | If this option is enabled no event is triggered<br>and no mail will be sent if the reminder is in<br>the past relative to the start date.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Calendar.html">Calendar</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Event.html">Event</a> |
| __File__ | calendar.properties |

---
| __Key__ | CHECK_AND_AVOID_SOLO_REMINDER_TRIGGER_EVENTS |
|:----------------|:--------|
| __Description__ | This option prevents the trigger and mail sending<br>if only a reminder has been changed. If the application<br>should inform about each change no matter what has been<br>changed in the object this option should be disabled.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Calendar.html">Calendar</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Event.html">Event</a> |
| __File__ | calendar.properties |

---
| __Key__ | com.openexchange.event.isEventQueueEnabled |
|:----------------|:--------|
| __Description__ | This option enables or disable the complete event handling for OX<br>The event handling is a essential Open-Xchange component which needs<br>to always run. This should only be set to false for debugging purposes.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Event.html">Event</a> |
| __File__ | event.properties |

---
| __Key__ | com.openexchange.event.eventQueueDelay |
|:----------------|:--------|
| __Description__ | This parameter set the delay in milliseconds when events are sent to <br>the subscribed services. Events for example are triggered when deleting<br>an appointment with attachment. The event then is for deleting the attachment.<br>If this value is increased more events may be in the queue that must be<br>executed. This can cause longer runtime delivering all those events. Lowering<br>this value may cause too often look into the queue without finding events<br>there to process.<br> |
| __Default__ | 60000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.event.isEventQueueEnabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Event.html">Event</a> |
| __File__ | event.properties |

---
