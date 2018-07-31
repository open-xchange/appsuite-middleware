---
title: Mail proxy configuration
---

It is now possible to configure a http based proxy for imap communication. 
This is especially important for environments which only allow connection to the internet via a proxy.
Before we dive into the configuration itself it is important to understand the drawback of this approach.
The used javax.mail library only supports not authenticated proxies. Therefore it is currently not possible to use a proxy with authentication.

To configure the proxy a few java system properties must be set. For example like this:

```properties
-Dmail.imap.proxy.host=proxy.local
-Dmail.imap.proxy.port=3128
-Dmail.smtp.proxy.host=proxy.local
-Dmail.smtp.proxy.port=3128
-Dmail.imaps.proxy.host=proxy.local
-Dmail.imaps.proxy.port=3128
-Dmail.smtps.proxy.host=proxy.local
-Dmail.smtps.proxy.port=3128
```

This will apply the proxy setting to all connection. Therefore it is advisable to exclude the primary account by configuring the nonProxyHost setting. E.g.:

```properties
com.openexchange.mail.proxy.nonProxyHosts = mail.server.local
```