---
title: Http proxy support
---

Depending on the network infrastructure, it may be the case that middleware nodes do not have direct access to the internet.
In those cases access is only possible via an HTTP proxy. With 7.10.0 it will be possible to configure such an HTTP proxy via
well known Java system properties:

* http.proxyHost
* http.proxyPort
* http.proxyUser
* http.proxyPassword
* http.nonProxyHosts
* https.proxyHost
* https.proxyPort
* https.proxyUser
* https.proxyPassword

For example this can be achieved by adding the properties to the `ox-scriptconf.sh` script:

```
[...]
JAVA_OPTS_OTHER=" -Dhttp.proxyHost=proxy.my.domain -Dhttps.proxyHost=proxy.my.domain -Dhttp.proxyPort=proxyPort -Dhttps.proxyPort=proxyPort"
[...]
```

It is recommended to whitelist internal systems by adding them to the `http.nonProxyHosts` property. E.g.:

```
-Dhttp.nonProxyHosts=localhost|127.*|[::1]|my.internal.system
```
