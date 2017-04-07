---
title: Realtime
---

This page shows all properties with the tag: Realtime

| __Key__ | com.openexchange.realtime.isTraceAllUsersEnabled |
|:----------------|:--------|
| __Description__ | Tracing:<br>Tracing is done by adding a unique tracer to the client message that enables you<br>to follow the path of a realtime tracer/message as it is handled by different<br>parts of the server stack. This done by logging a status message at the info<br>loglevel.<br><br>Enable tracing for all users.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Realtime.html">Realtime</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Logging.html">Logging</a> |
| __File__ | realtime.properties |

---
| __Key__ | com.openexchange.realtime.usersToTrace |
|:----------------|:--------|
| __Description__ | Enable tracing only for a set of users by using the userID@contextID notation e.g.: 1@1, 2@1, 3@1<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Realtime.html">Realtime</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Logging.html">Logging</a> |
| __File__ | realtime.properties |

---
| __Key__ | com.openexchange.realtime.numberOfRunLoops |
|:----------------|:--------|
| __Description__ | Specifies the number of run loops.<br><br>A high number of synchronous operations e.g. persisting documents might lead to timeouts for query<br>actions/stanzas that are still enqueued in the the runloop and wait to be handled.<br> |
| __Default__ | 16 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Realtime.html">Realtime</a> |
| __File__ | realtime.properties |

---
