---
title: Mail proxy configuration
---

It is possible to configure a HTTP based proxy for IMAP communication. This especially is important for environments which only allow connections to the internet via a proxy.

Before we dive into the configuration itself it is important to understand the drawback of this approach: The used javax.mail library only supports not authenticated proxies. Therefore it is currently not possible to use a proxy with authentication.

To configure the proxy a few java system properties must be set. For example:

```
-Dmail.imap.proxy.host=proxy.local
-Dmail.imap.proxy.port=3128
-Dmail.imaps.proxy.host=proxy.local
-Dmail.imaps.proxy.port=3128
-Dmail.smtp.proxy.host=proxy.local
-Dmail.smtp.proxy.port=3128
-Dmail.smtps.proxy.host=proxy.local
-Dmail.smtps.proxy.port=3128
```

This will apply the proxy setting to all connections! Therefore it is advisable to exclude the primary account by configuring the nonProxyHost setting. For example:

```
com.openexchange.mail.proxy.nonProxyHosts=mail.server.local
```