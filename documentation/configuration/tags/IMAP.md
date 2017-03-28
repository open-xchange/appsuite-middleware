---
title: IMAP
---

This page shows all properties with the tag: IMAP

| __Key__ | mail.debug |
|:----------------|:--------|
| __Description__ | The initial debug mode<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.from |
|:----------------|:--------|
| __Description__ | The return email address of the current user, used by the InternetAddress method getLocalAddress.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.address.strict |
|:----------------|:--------|
| __Description__ | The MimeMessage class uses the InternetAddress method parseHeader to parse headers in messages.<br>This property controls the strict flag passed to the parseHeader method.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.host |
|:----------------|:--------|
| __Description__ | The default host name of the mail server for both Stores and Transports. Used if the mail.protocol.host property isn't set.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.[protocol].host |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.store.protocol |
|:----------------|:--------|
| __Description__ | Specifies the default message access protocol. The Session method getStore() returns a Store object that implements this protocol.<br>By default the first Store provider in the configuration files is returned.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.transport.protocol |
|:----------------|:--------|
| __Description__ | Specifies the default message access protocol. The Session method getTransport() returns a Transport object that implements this protocol.<br>By default the first Transport provider in the configuration files is returned.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.user |
|:----------------|:--------|
| __Description__ | The default user name to use when connecting to the mail server. Used if the mail.protocol.user property isn't set.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.protocol.user |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.[protocol].class |
|:----------------|:--------|
| __Description__ | Specifies the fully qualified class name of the provider for the specified protocol.<br>Used in cases where more than one provider for a given protocol exists; this property can be used to specify which provider to use by default.<br>The provider must still be listed in a configuration file.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.[protocol].host |
|:----------------|:--------|
| __Description__ | The host name of the mail server for the specified protocol. Overrides the mail.host property.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.host |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.[protocol].port |
|:----------------|:--------|
| __Description__ | The port number of the mail server for the specified protocol. If not specified the protocol's default port number is used.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.[protocol].user |
|:----------------|:--------|
| __Description__ | The user name to use when connecting to mail servers using the specified protocol. Overrides the mail.user property.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.user |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.user |
|:----------------|:--------|
| __Description__ | Default user name for IMAP.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.user |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.host |
|:----------------|:--------|
| __Description__ | The IMAP server to connect to.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.host |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.port |
|:----------------|:--------|
| __Description__ | The IMAP server port to connect to, if the connect() method doesn't explicitly specify one.<br> |
| __Default__ | 143 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.partialfetch |
|:----------------|:--------|
| __Description__ | Controls whether the IMAP partial-fetch capability should be used.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.fetchsize |
|:----------------|:--------|
| __Description__ | Partial fetch size in bytes.<br> |
| __Default__ | 16K |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.connectiontimeout |
|:----------------|:--------|
| __Description__ | Socket connection timeout value in milliseconds.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.timeout |
|:----------------|:--------|
| __Description__ | Socket I/O timeout value in milliseconds.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.statuscachetimeout |
|:----------------|:--------|
| __Description__ | Timeout value in milliseconds for cache of STATUS command response. Default is 1000 (1 second). Zero disables cache.<br> |
| __Default__ | 1000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Cache.html">Cache</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.appendbuffersize |
|:----------------|:--------|
| __Description__ | Maximum size of a message to buffer in memory when appending to an IMAP folder.<br>If not set, or set to -1, there is no maximum and all messages are buffered. If set to 0, no messages are buffered.<br>If set to (e.g.) 8192, messages of 8K bytes or less are buffered, larger messages are not buffered.<br>Buffering saves cpu time at the expense of short term memory usage. If you commonly append very large messages to<br>IMAP mailboxes you might want to set this to a moderate value (1M or less).<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.connectionpoolsize |
|:----------------|:--------|
| __Description__ | Maximum number of available connections in the connection pool.<br> |
| __Default__ | 1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.connectionpooltimeout |
|:----------------|:--------|
| __Description__ | Timeout value in milliseconds for connection pool connections.<br> |
| __Default__ | 45000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.seperatestoreconnection |
|:----------------|:--------|
| __Description__ | Flag to indicate whether to use a dedicated store connection for store commands.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.allowreadonlyselect |
|:----------------|:--------|
| __Description__ | If false, attempts to open a folder read/write will fail if the SELECT command succeeds but indicates that the folder is READ-ONLY.<br>This sometimes indicates that the folder contents can'tbe changed, but the flags are per-user and can be changed, such as might be<br>the case for public shared folders. If true, such open attempts will succeed, allowing the flags to be changed.<br>The getMode method on the Folder object will return Folder.READ_ONLY in this case even though the open method specified Folder.READ_WRITE.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.auth.login.disable |
|:----------------|:--------|
| __Description__ | If true, prevents use of the non-standard AUTHENTICATE LOGIN command, instead using the plain LOGIN command.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.auth.plain.disable |
|:----------------|:--------|
| __Description__ | If true, prevents use of the AUTHENTICATE PLAIN command.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.localaddress |
|:----------------|:--------|
| __Description__ | Local address (host name) to bind to when creating the IMAP socket. Defaults to the address picked by the Socket class.<br>Should not normally need to be set, but useful with multi-homed hosts where it's important to pick a particular local address to bind to.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.localport |
|:----------------|:--------|
| __Description__ | Local port number to bind to when creating the IMAP socket. Defaults to the port number picked by the Socket class.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.sasl.enable |
|:----------------|:--------|
| __Description__ | If set to true, attempt to use the javax.security.sasl package to choose an authentication mechanism for login.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.sasl.mechanisms |
|:----------------|:--------|
| __Description__ | A space or comma separated list of SASL mechanism names to try to use.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.sasl.authorizationid |
|:----------------|:--------|
| __Description__ | The authorization ID to use in the SASL authentication. If not set, the authentication ID (user name) is used.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.message.class |
|:----------------|:--------|
| __Description__ | Class name of a subclass of com.sun.mail.pop3.POP3Message. The subclass can be used to handle (for example) non-standard Content-Type headers.<br>The subclass must have a public constructor of the form MyPOP3Message(Folder f, int msgno) throws MessagingException.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.store.maildir.autocreatedir |
|:----------------|:--------|
| __Description__ | Whether to auto-create specified maildir directory on maildir store connect or not.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.store.maildir.cachefolders |
|:----------------|:--------|
| __Description__ | Whether to cache maildir folder objects or not.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.store.maildir.checkmessagesizebeforeappend |
|:----------------|:--------|
| __Description__ | Whether to check quota limitations or not.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.store.maildir.quota.size |
|:----------------|:--------|
| __Description__ | Define the quota limit on STORAGE resource.<br> |
| __Default__ | 0 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.store.maildir.quota.count |
|:----------------|:--------|
| __Description__ | Define the quota limit on MESSAGE resource.<br> |
| __Default__ | 0 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.store.maildir.headerindex |
|:----------------|:--------|
| __Description__ | Whether to use a header index or not.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.store.maildir.inmemorysize |
|:----------------|:--------|
| __Description__ | Define the maximum RFC822 message size (in bytes) for being kept in memory completely. If a message exceeds this size, its corresponding<br>file is accessed with random access instead of reading file with an input stream.<br> |
| __Default__ | 131072 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.store.maildir.headercachemaxsize |
|:----------------|:--------|
| __Description__ | Define the maximum number of message headers that are kept in a maildir folder's header cache.<br> |
| __Default__ | 1000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.store.maildir.maxnumofheadercaches |
|:----------------|:--------|
| __Description__ | Define the maximum number of maildir folder header caches.<br> |
| __Default__ | 10 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.store.maildir.headercacheexpire |
|:----------------|:--------|
| __Description__ | Define the timeout for header caches (in milliseconds).<br> |
| __Default__ | 3600000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.address.strict |
|:----------------|:--------|
| __Description__ | The mail.mime.address.strict session property controls the parsing of address headers.<br>By default, strict parsing of address headers is done. If this property is set to "false",<br>strict parsing is not done and many illegal addresses that sometimes occur in real messages are allowed.<br>See the InternetAddress class for details.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.charset |
|:----------------|:--------|
| __Description__ | The mail.mime.charset System property can be used to specify the default MIME charset to use for encoded words and text parts that don't<br>otherwise specify a charset. Normally, the default MIME charset is derived from the default Java charset, as specified in the file.encoding<br>System property. Most applications will have no need to explicitly set the default MIME charset.<br>In cases where the default MIME charset to be used for mail messages is different than the charset used for files stored on the system,<br>this property should be set.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.decodetext.strict |
|:----------------|:--------|
| __Description__ | The mail.mime.decodetext.strict property controls decoding of MIME encoded words.<br>The MIME spec requires that encoded words start at the beginning of a whitespace separated word.<br>Some mailers incorrectly include encoded words in the middle of a word.<br>If the mail.mime.decodetext.strict System property is set to "false", an attempt will be made to decode these illegal encoded words.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.encodeeol.strict |
|:----------------|:--------|
| __Description__ | The mail.mime.encodeeol.strict property controls the choice of Content-Transfer-Encoding for MIME parts that are not of type "text".<br>Often such parts will contain textual data for which an encoding that allows normal end of line conventions is appropriate.<br>In rare cases, such a part will appear to contain entirely textual data, but will require an encoding that preserves CR and LF characters<br>without change. If the mail.mime.encodeeol.strict System property is set to "true", such an encoding will be used when necessary.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.decodefilename |
|:----------------|:--------|
| __Description__ | If set to "true", the getFileName method uses the MimeUtility method decodeText to decode any non-ASCII characters in the filename.<br>Note that this decoding violates the MIME specification, but is useful for interoperating with some mail clients that use this convention.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.encodefilename |
|:----------------|:--------|
| __Description__ | If set to "true", the setFileName method uses the MimeUtility method encodeText to encode any non-ASCII characters in the filename.<br>Note that this encoding violates the MIME specification, but is useful for interoperating with some mail clients that use this convention.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.decodeparameters |
|:----------------|:--------|
| __Description__ | If set to "true", non-ASCII parameters in a ParameterList, e.g., in a Content-Type header, will be encoded as specified by RFC 2231.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.encodeparameters |
|:----------------|:--------|
| __Description__ | If set to "true", non-ASCII parameters in a ParameterList, e.g., in a Content-Type header, will be decoded as specified by RFC 2231.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.ignoremissingendboundary |
|:----------------|:--------|
| __Description__ | Normally, when parsing a multipart MIME message, a message that is missing the final end boundary line is not considered an error.<br>The data simply ends at the end of the input. Note that messages of this form violate the MIME specification.<br>If the property mail.mime.multipart.ignoremissingendboundary is set to false, such messages are considered an error and a MesagingException<br>will be thrown when parsing such a message.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.ignoremissingboundaryparameter |
|:----------------|:--------|
| __Description__ | If the Content-Type header for a multipart content does not have a boundary parameter, the multipart parsing code will look for the first<br>line in the content that looks like a boundary line and extract the boundary parameter from the line.<br>If this property is set to "false", a MessagingException will be thrown if the Content-Type header doesn't specify a boundary parameter.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.base64.ignoreerrors |
|:----------------|:--------|
| __Description__ | If set to "true", the BASE64 decoder will ignore errors in the encoded data, returning EOF.<br>This may be useful when dealing with improperly encoded messages that contain extraneous data at the end of the encoded stream.<br>Note however that errors anywhere in the stream will cause the decoder to stop decoding so this should be used with extreme caution.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.foldtext |
|:----------------|:--------|
| __Description__ | If set to "true", header fields containing just text such as the Subject and Content-Description header fields, and long parameter values<br>in structured headers such as Content-Type will be folded (broken into 76 character lines) when set and unfolded when read.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.setcontenttypefilename |
|:----------------|:--------|
| __Description__ | If set to "true", the setFileName method will also set the name parameter on the Content-Type header to the specified filename.<br>This supports interoperability with some old mail clients.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.setdefaulttextcharset |
|:----------------|:--------|
| __Description__ | When updating the headers of a message, a body part with a text content type but no charset parameter will have<br>a charset parameter added to it if this property is set to "true".<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.alternates |
|:----------------|:--------|
| __Description__ | A string containing other email addresses that the current user is known by. The MimeMessage reply method will eliminate any of<br>these addresses from the recipient list in the message it constructs, to avoid sending the reply back to the sender.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.replyallcc |
|:----------------|:--------|
| __Description__ | If set to "true", the MimeMessage reply method will put all recipients except the original sender in the Cc list of the newly constructed message.<br>Normally, recipients in the To header of the original message will also appear in the To list of the newly constructed message.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | com.openexchange.imap[.primary].imapSort |
|:----------------|:--------|
| __Description__ | Define where to sort emails: Value "imap" to let the  IMAP Server sort (faster but less reliable).<br>Leave blank or fill in value "application" to sort within application (slower but good quality).<br>The sorting is done on IMAP server if a mailbox' size exceeds the mailFetchLimit as defined in<br>mail.properties.<br>NOTE:<br>This value is going to be set to "application" if IMAP server capabilities do not contain string "SORT".<br>Moreover, please also refer to property "com.openexchange.imap.fallbackOnFailedSORT" to specify how to react to a possible "NO" response.<br>Default is "imap"<br> |
| __Default__ | imap |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].imapSearch |
|:----------------|:--------|
| __Description__ | Define where to search for emails:<br>- Use value "imap" to let the IMAP Server search. The search is done on IMAP server if a mailbox' size exceeds the mailFetchLimit as defined in mail.properties.<br>- Use value "force-imap" to let the IMAP Server search in every case.<br>- Leave blank or fill in value "application" to search within application.<br>Default is "force-imap"<br> |
| __Default__ | force-imap |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.umlautFilterThreshold |
|:----------------|:--------|
| __Description__ | Specify the threshold for number of search results returned by IMAP server for which manual umlauts-filtering<br>will be applied. If less than or equal to zero, no manual filtering will be applied.<br>Default value is 50.<br> |
| __Default__ | 50 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].imapFastFetch |
|:----------------|:--------|
| __Description__ | This property determines whether a fast fetch is performed on large mail<br>folders or not. Although the fetch is fast on IMAP side, a lot of data is<br>transfered during reading response which cause a temporary memory peak.<br>If disabled only the necessary fields are used as command arguments,<br>which is slower but needs less memory.<br>NOTE: See property "imapMessageFetchLimit" to know which mail folders are<br>treated as large mail folders<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].imapSupportsACL |
|:----------------|:--------|
| __Description__ | Define if IMAP server supports ACLs. Possible values: true/false/auto<br>NOTE: Value "auto" means to use server-defined ACL support as indicated<br>through response to IMAP command "CAPABILITY"<br> |
| __Default__ | auto |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].imapTimeout |
|:----------------|:--------|
| __Description__ | Define the socket read timeout value in milliseconds. A value less than<br>or equal to zero is infinite timeout. See also mail.imap.timeout<br> |
| __Default__ | 50000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].imapConnectionTimeout |
|:----------------|:--------|
| __Description__ | Define the socket connect timeout value in milliseconds. A value less than<br>or equal to zero is infinite timeout. See also mail.imap.connectiontimeout<br> |
| __Default__ | 20000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].imapTemporaryDown |
|:----------------|:--------|
| __Description__ | Define the amount of time in milliseconds an IMAP server is treated as being temporary down.<br>An IMAP server is treated as being temporary down if a socket connect fails. Further requests to<br>the affected IMAP server are going to be denied for the specified amount of time.<br>A value less or equal to zero disables this setting.<br> |
| __Default__ | 10000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].imapAuthEnc |
|:----------------|:--------|
| __Description__ | Define the encoding for IMAP authentication<br> |
| __Default__ | UTF-8 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.User2ACLImpl |
|:----------------|:--------|
| __Description__ | Name of the class that implements User2ACL, their alias or "auto" to use auto-detection.<br>Currently known aliases: Cyrus, Courier, Dovecot, and Sun (Sun Java(tm) System Messaging Server)<br> |
| __Default__ | auto |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].blockSize |
|:----------------|:--------|
| __Description__ | IMAP operations which shall be applied to a number of messages which exceeds the block size<br>are performed in blocks. Example: A folder containing thousands of messages shall be cleared.<br>To avoid the risk of an IMAP timeout when trying to delete all messages at once, the messages<br>are deleted in block size portions.<br>A block size equal to or less than zero means no block size.<br> |
| __Default__ | 1000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.spamHandler |
|:----------------|:--------|
| __Description__ | Define the registration name of the appropriate spam handler to use<br>Note: This value gets overwritten by "com.openexchange.spamhandler.name" property<br> |
| __Default__ | DefaultSpamHandler |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.spamhandler.name |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spam_Handler.html">Spam Handler</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].propagateClientIPAddress |
|:----------------|:--------|
| __Description__ | Whether client's IP address should be propagated by a NOOP command; e.g. "A01 NOOP <CLIENT_IP>"<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].propagateHostNames |
|:----------------|:--------|
| __Description__ | Configure a comma-separated list of external IMAP server's host names which should receive client's IP address by a NOOP command, too<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].maxNumConnections |
|:----------------|:--------|
| __Description__ | The max. number of connection allowed being established for a user to an IMAP server. Less than or equal to zero means infinite.<br>Please also consider "com.openexchange.imap.storeContainerType".<br><br>Note: This setting overrides possibles restrictions specified through property "com.openexchange.imap.maxNumExternalConnections" if<br>this property's value is less than the other one.<br> |
| __Default__ | 0 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.imap.storeContainerType |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.storeContainerType |
|:----------------|:--------|
| __Description__ | An IMAP connection cache acts a temporary keep-alive cache for already connected resources to an IMAP server's account.<br>Meaning it keeps a resource open/connected for a short amount of time (see "com.openexchange.mail.mailAccessCacheIdleSeconds")<br>and performs a "close elapsed ones" run periodically (see "com.openexchange.mail.mailAccessCacheShrinkerSeconds").<br><br>In addition to that behavior there are two modes of operation - bounded and unbounded.<br><br>For an unbounded cache, set this property to "unbounded".<br>Thus a user is allowed to establish as many connections to his IMAP account as demanded by his active clients (Web UI, EAS, Outlook OXtender, etc.).<br><br>A bounded cache allows only as many concurrently opened resources as specified through "com.openexchange.imap.maxNumConnections" property.<br>Taking the wording "resource" was chosen by intention, since two types of resource abstractions exist:<br>IMAP store and IMAP protocol (an authenticated login's socket connection).<br><br>The default setting "boundary-aware" considers an "IMAP store" as limited resources to an IMAP server.<br>The vague thing about IMAP store is that it maintains a connection pool internally by itself.<br>Thus it is possible that there are actually more active socket connections open than specified,<br>because an IMAP store is allowed to open further connections when needed;<br>e.g. when accessing another IMAP folder while INBOX has been opened, too.<br>Practical experience showed that there will be at max.: "com.openexchange.imap.maxNumConnections" + 1<br><br>The setting "non-caching" does an exact mapping of resource to an established/authenticated socket connection to the IMAP account.<br>It is named "non-caching" as it does no caching on its own, but delegates it to a custom queuing 'com.sun.mail.imap.IMAPStore' class.<br>Thus an exact limitation of connected socket connections ('com.sun.mail.imap.protocol.IMAPProtocol' instances) is achieved.<br>Specifying a quite small limitation - let's say "1" - arises the possibility that JavaMail gets dead-locked by itself.<br>E.g. an IMAP store attempts to create a second connection. That attempt may get stalled as it waits for itself to free the already<br>acquired connection which never happens.<br>So, please use this exact mapping only if you specify a reasonable limitation.<br> |
| __Default__ | boundary-aware |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.maxNumExternalConnections |
|:----------------|:--------|
| __Description__ | Configure the max. number of concurrent connections which are allowed being established to a subscribed/external IMAP account.<br>Notation is a comma-separated list of: <host> + ':' + <max-count>; e.g.:<br>    com.openexchange.imap.maxNumExternalConnections=imap.host1.com:4, imap.host2.com:6<br>For convenience a max-count can be specified which applies to all subscribed/external IMAP accounts; e.g.:<br>    com.openexchange.imap.maxNumExternalConnections=4<br>Zero or less is interpreted as unlimited.<br>If not set, unlimited concurrent connections are allowed.<br> |
| __Default__ | imap.gmail.com:2,imap.googlemail.com:2 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].enableTls |
|:----------------|:--------|
| __Description__ | Enables the use of the STARTTLS command (if supported by the server) to switch the connection to a TLS-protected connection.<br>Note: This property is statically used in IMAP authentication bundle<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.invalidMailboxNameCharacters |
|:----------------|:--------|
| __Description__ | Specifies a space-separated list of characters that are not allowed to be contained in a mailbox name;<br>e.g. >>com.openexchange.imap.invalidMailboxNameCharacters="; / . &#124; \\"<<<br>Default is empty.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].allowFolderCaches |
|:----------------|:--------|
| __Description__ | Enables/disables caching of IMAP folders.<br>Default is true.<br>Note: Only disable IMAP folder cache if you certainly know what you are doing.<br>Disabling that cache may result in noticeable performance decrease.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].ssl.protocols |
|:----------------|:--------|
| __Description__ | Specifies the SSL protocols that will be enabled for SSL connections. The property value is a whitespace separated list of tokens.<br>Default is empty<br><br>Note: This property is statically used in IMAP authentication bundle<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].ssl.ciphersuites |
|:----------------|:--------|
| __Description__ | Specifies the SSL cipher suites that will be enabled for SSL connections. The property value is a whitespace separated list of tokens.<br>Check "http://<ox-grizzly-hostname>:<ox-grizzly-port>/stats/diagnostic?param=ciphersuites" to check available cipher suites.<br>Default value is empty (fall-back to current JVM's default SSL cipher suite)<br><br>Note: This property is statically used in IMAP authentication bundle<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.maxMailboxNameLength |
|:----------------|:--------|
| __Description__ | The max. length of a mailbox name<br> |
| __Default__ | 60 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.invalidMailboxNameCharacters |
|:----------------|:--------|
| __Description__ | Specifies a space-separated list of characters that are not allowed to be contained in a mailbox name;<br>e.g. >>com.openexchange.imap.invalidMailboxNameCharacters="; / . &#124; \\"<<<br>Default is empty.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.namespacePerUser |
|:----------------|:--------|
| __Description__ | This option controls whether there it is expected to have a dedicated NAMESPACE per user or not.<br>In case of "true" each mailbox account on associated IMAP server is allowed to have its own NAMESPACE set; might be "" (root) or "INBOX.".<br>Otherwise for "false" every mailbox is assumed to have the same NAMESPACE set.<br><br>This influences the way Open-Xchange Server detects & caches NAMESPACE information; either on a per user basis (more IMAP traffic) or<br>globally (only requested once).<br><br>Do not touch unless you certainly know IMAP sever's behavior.<br><br>Default is "true"<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.rootSubfoldersAllowed |
|:----------------|:--------|
| __Description__ | If either "true" or "false" set, it enforces whether to assume root sub-folder capability for primary account.<br>If not set, root sub-folder capability is probed through creating a temporary folder.<br><br>Default is empty (probe through temp. folder)<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.allowSORTDISPLAY |
|:----------------|:--------|
| __Description__ | Specifies if "SORT=DISPLAY" IMAP extension is supposed to be considered when returning a mail listing sorted by From/To<br><br>Default is "false"<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.fallbackOnFailedSORT |
|:----------------|:--------|
| __Description__ | Specifies if a fall-back to in-application sort is supposed to be performed in case the IMAP server quits with a "NO" response for the<br>issued SORT command.<br>Note: Doing in-application sort contains the danger of utilizing too much memory (especially for big mailboxes).<br><br>Default is "false"<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].auditLog.enabled |
|:----------------|:--------|
| __Description__ | Enables the audit log for issued IMAP commands via 'com.sun.mail.imap.AuditLog' class.<br><br>Accepts the "primary." suffix to only enable for primary account;<br>e.g. "com.openexchange.imap.primary.auditLog.enabled=true"<br><br>Default is "false"<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.initWithSpecialUse |
|:----------------|:--------|
| __Description__ | Specifies whether the primary mail account should be initialized with special-use folders from the imap server in case no standard folder names are configured.<br><br>Default is 'true'<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.greeting.host.regex |
|:----------------|:--------|
| __Description__ | Specifies the regular expression to use to extract the host name/IP address information out of the greeting string advertised by primary<br>IMAP server. Only applicable for primary IMAP server! Default is empty.<br><br>The regular expression is supposed to be specified in Java notation: http://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html<br><br>Moreover, either the complete regex is considered or in case a capturing group is present that group will be preferred.<br>I.e. "Dovecot at ([0-9a-zA-Z._-]\*) is ready", then the capturing group is supposed to extract the host name/IP addres information<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
