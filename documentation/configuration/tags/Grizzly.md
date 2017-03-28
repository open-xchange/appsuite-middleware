---
title: Grizzly
---

This page shows all properties with the tag: Grizzly

| __Key__ | com.openexchange.http.grizzly.wsTimeoutMillis |
|:----------------|:--------|
| __Description__ | Specifies the Web Socket timeout in milliseconds<br> |
| __Default__ | 900000 |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.websockets.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Websockets.html">Websockets</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.http.grizzly.sessionExpiryCheckInterval |
|:----------------|:--------|
| __Description__ | Specifies the interval in seconds when to check for expired/invalid HTTP sessions<br>This value should be aligned to property "com.openexchange.servlet.maxInactiveInterval"<br>that defines how long (in seconds) a HTTP session may stay idle/inactive until considered<br>as invalid<br> |
| __Default__ | 60 |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Session.html">Session</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.http.grizzly.maxNumberOfConcurrentRequests |
|:----------------|:--------|
| __Description__ | Specifies the number of concurrent HTTP requests that are allowed being processed.<br>Those requests exceeding that limit will encounter a 503 "The server is temporary overloaded..." status code and accompanying error page<br>A value of less than or equal to 0 (zero) effectively disables that limitation.<br>The chosen value for this property should be aligned to the configured "ulimit" of the backing operating system. E.g. having "ulimit" set<br>to 8,192 (given that JVM is the only main process running for OS user) implies that ~6,000 should be considered for this property leaving<br>some room for threads not associated with an HTTP request.<br> |
| __Default__ | 0 |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.http.grizzly.keepAlive |
|:----------------|:--------|
| __Description__ | Enables or disables SO_KEEPALIVE.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.http.grizzly.tcpNoDelay |
|:----------------|:--------|
| __Description__ | Enables/disables TCP_NODELAY (disable/enable Nagle's algorithm).<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.http.grizzly.readTimeoutMillis |
|:----------------|:--------|
| __Description__ | Specifies the read timeout, in milliseconds. A timeout of zero is interpreted as an infinite timeout.<br> |
| __Default__ | 60000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.http.grizzly.writeTimeoutMillis |
|:----------------|:--------|
| __Description__ | Specifies the write timeout, in milliseconds. A timeout of zero is interpreted as an infinite timeout.<br> |
| __Default__ | 60000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.http.grizzly.hasCometEnabled |
|:----------------|:--------|
| __Description__ | Comet is an umbrella term used to describe a technique allowing web browser to<br>receive almost real time updates from the server. The two most common<br>approaches are long polling and streaming. Long polling differs from streaming<br>in that each update from the server ultimately results in another follow up<br>request from the client.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
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
| __Key__ | com.openexchange.http.grizzly.hasJMXEnabled |
|:----------------|:--------|
| __Description__ | Enabling grizzly monitoring via JMX.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Monitoring.html">Monitoring</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.http.grizzly.maxHttpHeaderSize |
|:----------------|:--------|
| __Description__ | The maximum header size for an HTTP request in bytes. Make sure to increase<br>this value for all components of your infrastructure when you are forced to<br>deal with enormous headers. For Apache as our default balancer see<br>http://httpd.apache.org/docs/current/mod/core.html#limitrequestfieldsize<br> |
| __Default__ | 8192 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.http.grizzly.hasSSLEnabled |
|:----------------|:--------|
| __Description__ | Enable secure network listener.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SSL.html">SSL</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.http.grizzly.enabledCipherSuites |
|:----------------|:--------|
| __Description__ | Comma-separated list of cipher suites that should be used for secure connections.<br>See https://www.openssl.org/docs/manmaster/apps/ciphers.html<br>No value means system-default.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SSL.html">SSL</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.http.grizzly.keystorePath |
|:----------------|:--------|
| __Description__ | Path to keystore containing certificate for secure connections.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SSL.html">SSL</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.http.grizzly.keystorePassword |
|:----------------|:--------|
| __Description__ | Password for keystore containing certificate for secure connections.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SSL.html">SSL</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | grizzly.properties |

---
