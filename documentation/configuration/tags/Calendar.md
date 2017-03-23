---
title: Calendar
---

This page shows all properties with the tag: Calendar

| __Key__ | CACHED_ITERATOR_FAST_FETCH |
|:----------------|:--------|
| __Description__ | By enabling the option CACHED_ITERATOR_FAST_FETCH you<br>can define the numbers of pre fetched results with<br>the parameter MAX_PRE_FETCH.<br><br>This means that MAX_PRE_FETCH results are gathered in one<br>SQL query instead of MAX_PRE_FETCH single SQL queries.<br>Normally higher values result in more performance if this<br>option is enabled.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | MAX_PRE_FETCH |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Calendar.html">Calendar</a> |
| __File__ | calendar.properties |

---
| __Key__ | MAX_PRE_FETCH |
|:----------------|:--------|
| __Description__ | By enabling the option CACHED_ITERATOR_FAST_FETCH you<br>can define the numbers of pre fetched results with<br>the parameter MAX_PRE_FETCH.<br><br>This means that MAX_PRE_FETCH results are gathered in one<br>SQL query instead of MAX_PRE_FETCH single SQL queries.<br>Normally higher values result in more performance if this<br>option is enabled.<br> |
| __Default__ | 20 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | CACHED_ITERATOR_FAST_FETCH |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Calendar.html">Calendar</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | calendar.properties |

---
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
| __Key__ | MAX_OPERATIONS_IN_RECURRENCE_CALCULATIONS |
|:----------------|:--------|
| __Description__ | This options specifies a maximum count of loop iterations<br>for a given recurrence pattern. When this limit is reached<br>the server stops processing the recurrence pattern and spews<br>out lots of error information. A value equal to or less than<br>zero omits this property; meaning no limit on processing the<br>recurrence pattern.<br> |
| __Default__ | 49950 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Calendar.html">Calendar</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | calendar.properties |

---
| __Key__ | com.openexchange.calendar.seriesconflictlimit |
|:----------------|:--------|
| __Description__ | This boolean option switches on/off the limitation for the<br>conflict search for a series to 1 year in the future. This<br>means, that a new/changed series will not conflict with<br>appointments which are later than one year after the<br>creation/change of the appointment.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Calendar.html">Calendar</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | calendar.properties |

---
| __Key__ | com.openexchange.calendar.undefinedstatusconflict |
|:----------------|:--------|
| __Description__ | This boolean option switches on/off, if an appointment<br>should conflict for a user if he/she has not yet accepted<br>or denied the appointment.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Calendar.html">Calendar</a> |
| __File__ | calendar.properties |

---
