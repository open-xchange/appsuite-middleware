---
title: Event
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
