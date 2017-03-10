# Grizzly

| __Key__ | com.openexchange.http.grizzly.wsTimeoutMillis |
|:----------------|:--------|
| __Description__ | Specifies the Web Socket timeout in milliseconds<br> |
| __Default__ | 900000 |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.websockets.enabled |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.http.grizzly.sessionExpiryCheckInterval |
|:----------------|:--------|
| __Description__ | Specifies the interval in seconds when to check for expired/invalid HTTP sessions<br>This value should be aligned to property "com.openexchange.servlet.maxInactiveInterval"<br>that defines how long (in seconds) a HTTP session may stay idle/inactive until considered<br>as invalid<br> |
| __Default__ | 60 |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.http.grizzly.maxNumberOfConcurrentRequests |
|:----------------|:--------|
| __Description__ | Specifies the number of concurrent HTTP requests that are allowed being processed.<br>Those requests exceeding that limit will encounter a 503 "The server is temporary overloaded..." status code and accompanying error page<br>A value of less than or equal to 0 (zero) effectively disables that limitation.<br>The chosen value for this property should be aligned to the configured "ulimit" of the backing operating system. E.g. having "ulimit" set<br>to 8,192 (given that JVM is the only main process running for OS user) implies that ~6,000 should be considered for this property leaving<br>some room for threads not associated with an HTTP request.<br> |
| __Default__ | 0 |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | grizzly.properties |

---
