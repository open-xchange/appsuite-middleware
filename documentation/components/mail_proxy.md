---
title: Mail proxy configuration
---

It is possible to configure a HTTP based proxy for IMAP communication. This especially is important for environments which only allow connections to the internet via a proxy.
Before we dive into the configuration itself it is important to understand the drawback of this approach: The used javax.mail library only supports not authenticated proxies. 
Therefore it is currently not possible to use a proxy with authentication.

To configure the proxy a few java system properties must be set. For example:

```
# imap
-Dmail.imap.proxy.host=proxy.local
-Dmail.imap.proxy.port=3128
-Dmail.imaps.proxy.host=proxy.local
-Dmail.imaps.proxy.port=3128
# smtp
-Dmail.smtp.proxy.host=proxy.local
-Dmail.smtp.proxy.port=3128
-Dmail.smtps.proxy.host=proxy.local
-Dmail.smtps.proxy.port=3128
```

This will apply the proxy setting to all imap and smtp connections. It many scenarios it is advisable to exclude the primary mail account by also 
configuring the 'nonProxyHosts' java system properties. Please note that those properties are in opposition to the other properties ox specific properties. 
For example:

```
-Dmail.imap.proxy.nonProxyHosts=my.local.imap.server
-Dmail.smtps.proxy.nonProxyHosts=my.local.smtp.server
```

This properties are applied to both secure and non-secure connections.