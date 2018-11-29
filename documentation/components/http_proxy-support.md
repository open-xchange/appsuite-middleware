---
title: Proxy support
---

# Introduction

Depending on the network infrastructure, it may be the case that middleware nodes do not have direct access to the internet.
In those cases access is only possible via a proxy. It is now possible to configure such a proxy via well known Java system properties.
The following protocols are supported with authentication:

* http
* https

Additionally the following protocols are supported without authentication:

* imap
* imaps
* smtp
* smtps

For each protocoll exists a host and port property. Additionally a user and a password property can be configured for each protocol with authentication support.
There also exist a property per protocol group to configure a nonProxyHost list.

The complete list of proxy properties:

## http & https
* http.proxyHost
* http.proxyPort
* http.proxyUser
* http.proxyPassword
* http.nonProxyHosts
* https.proxyHost
* https.proxyPort
* https.proxyUser
* https.proxyPassword

## imap & imaps
* mail.imap.proxy.host
* mail.imap.proxy.port
* mail.imaps.proxy.host
* mail.imaps.proxy.port
* mail.imap.proxy.nonProxyHosts

## smtp & smtps
* mail.smtp.proxy.host
* mail.smtp.proxy.port
* mail.smtps.proxy.host
* mail.smtps.proxy.port
* mail.smtp.proxy.nonProxyHosts

# Example

For example to configure the http proxy one can add the properties to the `ox-scriptconf.sh` script:

```
[...]
JAVA_OPTS_OTHER=" -Dhttp.proxyHost=proxy.my.domain -Dhttps.proxyHost=proxy.my.domain -Dhttp.proxyPort=proxyPort -Dhttps.proxyPort=proxyPort"
[...]
```

It is recommended to whitelist internal systems by adding them to the nonProxyHosts properties. E.g. to the `http.nonProxyHosts` property:

```
-Dhttp.nonProxyHosts=localhost|127.*|[::1]|my.internal.system
```
