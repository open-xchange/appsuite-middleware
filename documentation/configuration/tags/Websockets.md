---
title: Websockets
---

This page shows all properties with the tag: Websockets

| __Key__ | com.openexchange.http.grizzly.wsTimeoutMillis |
|:----------------|:--------|
| __Description__ | Specifies the Web Socket timeout in milliseconds<br> |
| __Default__ | 900000 |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.websockets.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Websockets.html">Websockets</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.http.grizzly.hasWebSocketsEnabled |
|:----------------|:--------|
| __Description__ | Bi-directional, full-duplex communications channels over a single TCP connection.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Websockets.html">Websockets</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.websockets.enabled |
|:----------------|:--------|
| __Description__ | The main switch to enable/disable Web Sockets. That property is responsive to config-cascade<br>and reloadable as well.<br> |
| __Default__ | true |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Websockets.html">Websockets</a> |
| __File__ | websockets.properties |

---
| __Key__ | com.openexchange.websockets.grizzly.remote.delayDuration |
|:----------------|:--------|
| __Description__ | The time in milliseconds a message (that is supposed to be transferred to a remote cluster member)<br>is queued in buffer to await & aggregate equal messages that arrive during that time<br> |
| __Default__ | 1000 |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Websockets.html">Websockets</a> |
| __File__ | websockets.properties |

---
| __Key__ | com.openexchange.websockets.grizzly.remote.maxDelayDuration |
|:----------------|:--------|
| __Description__ | The time in milliseconds a message (that is supposed to be transferred to a remote cluster member)<br>is at max. queued in buffer to await & aggregate equal messages that arrive during that time.<br>So, even if there was an equal message recently, message is flushed from queue to avoid holding back<br>a message forever in case there are frequent equal messages.<br> |
| __Default__ | 3000 |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Websockets.html">Websockets</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | websockets.properties |

---
| __Key__ | com.openexchange.websockets.grizzly.remote.timerFrequency |
|:----------------|:--------|
| __Description__ | The frequency/delay in milliseconds when the buffering queue will be checked for due<br>"remote" messages (the ones exceeding delayDuration in queue)<br> |
| __Default__ | 500 |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Websockets.html">Websockets</a> |
| __File__ | websockets.properties |

---
