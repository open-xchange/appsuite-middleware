---
title: POP3
---

This page shows all properties with the tag: POP3

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
| __Key__ | mail.imap.allowreadonlyselect |
|:----------------|:--------|
| __Description__ | If false, attempts to open a folder read/write will fail if the SELECT command succeeds but indicates that the folder is READ-ONLY.<br>This sometimes indicates that the folder contents can'tbe changed, but the flags are per-user and can be changed, such as might be<br>the case for public shared folders. If true, such open attempts will succeed, allowing the flags to be changed.<br>The getMode method on the Folder object will return Folder.READ_ONLY in this case even though the open method specified Folder.READ_WRITE.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.user |
|:----------------|:--------|
| __Description__ | Default user name for POP3.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.user |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.host |
|:----------------|:--------|
| __Description__ | The POP3 server to connect to.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.host |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.port |
|:----------------|:--------|
| __Description__ | The POP3 server port to connect to, if the connect() method doesn't explicitly specify one.<br> |
| __Default__ | 110 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.connectiontimeout |
|:----------------|:--------|
| __Description__ | Socket connection timeout value in milliseconds.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.timeout |
|:----------------|:--------|
| __Description__ | Socket I/O timeout value in milliseconds.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.rsetbeforequit |
|:----------------|:--------|
| __Description__ | Send a POP3 RSET command when closing the folder, before sending the QUIT command. Useful with POP3 servers that implicitly mark all<br>messages that are read as "deleted"; this will prevent such messages from being deleted and expunged unless the client requests so.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
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
| __Key__ | mail.pop3.localaddress |
|:----------------|:--------|
| __Description__ | Local address (host name) to bind to when creating the POP3 socket. Defaults to the address picked by the Socket class.<br>Should not normally need to be set, but useful with multi-homed hosts where it's important to pick a particular local address to bind to.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.localport |
|:----------------|:--------|
| __Description__ | Local port number to bind to when creating the POP3 socket. Defaults to the port number picked by the Socket class.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.apop.enable |
|:----------------|:--------|
| __Description__ | If set to true, use APOP instead of USER/PASS to login to the POP3 server, if the POP3 server supports APOP.<br>APOP sends a digest of the password rather than the clear text password.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.disabletop |
|:----------------|:--------|
| __Description__ | If set to true, the POP3 TOP command will not be used to fetch message headers. This is useful for POP3 servers that don't properly implement the TOP command,<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.forgettopheaders |
|:----------------|:--------|
| __Description__ | If set to true, the headers that might have been retrieved using the POP3 TOP command will be forgotten and replaced by headers retrieved as part of the POP3 RETR command.<br>Some servers, such as some version of Microsft Exchange, will return slightly different headers each time the TOP or RETR command is used.<br>To allow the POP3 provider to properly parse the message content returned from the RETR command, the headers also returned by the RETR command must be used.<br>Setting this property to true will cause these headers to be used, even if they differ from the headers returned previously as a result of using the TOP command.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
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
| __Key__ | com.openexchange.pop3.pop3Timeout |
|:----------------|:--------|
| __Description__ | Define the socket read timeout value in milliseconds. A value less than<br>or equal to zero is infinite timeout. See also mail.smtp.timeout<br>Default is 50000<br> |
| __Default__ | 50000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | pop3.properties |

---
| __Key__ | com.openexchange.pop3.pop3ConnectionTimeout |
|:----------------|:--------|
| __Description__ | Define the socket connect timeout value in milliseconds. A value less<br>or equal to zero is infinite timeout. See also mail.smtp.connectiontimeout<br>Default is 20000<br> |
| __Default__ | 20000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | pop3.properties |

---
| __Key__ | com.openexchange.pop3.pop3TemporaryDown |
|:----------------|:--------|
| __Description__ | Define the amount of time in milliseconds a POP3 server is treated as being temporary down.<br>A POP3 server is treated as being temporary down if a socket connect fails. Further requests to<br>the affected POP3 server are going to be denied for the specified amount of time.<br>A value less or equal to zero disables this setting.<br> |
| __Default__ | 10000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | pop3.properties |

---
| __Key__ | com.openexchange.pop3.pop3AuthEnc |
|:----------------|:--------|
| __Description__ | Define the encoding for POP3 authentication<br> |
| __Default__ | UTF-8 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | pop3.properties |

---
| __Key__ | com.openexchange.pop3.spamHandler |
|:----------------|:--------|
| __Description__ | Define the registration name of the appropriate spam handler to use<br>Note: This value gets overwritten by "com.openexchange.spamhandler.name" property<br> |
| __Default__ | DefaultSpamHandler |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.spamhandler.name |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | pop3.properties |

---
| __Key__ | com.openexchange.pop3.pop3ConnectionIdleTime |
|:----------------|:--------|
| __Description__ | Define the amount of time in milliseconds an established POP3 connection is kept<br>open although being idle. Since some POP3 servers limit the time period in which<br>connections may be opened/closed, this property allows to keep the connection open<br>to avoid an error on a subsequent login.<br>This property overwrites default connection idle time specified through property<br>"com.openexchange.mail.mailAccessCacheIdleSeconds".<br> |
| __Default__ | 300000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | pop3.properties |

---
| __Key__ | com.openexchange.pop3.pop3BlockSize |
|:----------------|:--------|
| __Description__ | Specify the number of messages (positive integer!) which are allowed to be processed at once.<br>Default is 100.<br>Zero or negative value is defaulted to 100.<br> |
| __Default__ | 100 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | pop3.properties |

---
| __Key__ | com.openexchange.pop3.allowPing |
|:----------------|:--------|
| __Description__ | Whether ping operation is allowed for POP3 account<br>Many POP3 account limit number of allowed login attempts in a certain time interval<br>Default is false<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | pop3.properties |

---
| __Key__ | com.openexchange.pop3.logDeniedPing |
|:----------------|:--------|
| __Description__ | Whether denied ping operation shall be indicated as a warning to client<br>Only effective if "com.openexchange.pop3.allowPing" is set to false.<br>Default is true<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | pop3.properties |

---
| __Key__ | com.openexchange.pop3.ssl.protocols |
|:----------------|:--------|
| __Description__ | Specifies the SSL protocols that will be enabled for SSL connections. The property value is a whitespace separated list of tokens.<br>Default is empty<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | pop3.properties |

---
| __Key__ | com.openexchange.pop3.ssl.ciphersuites |
|:----------------|:--------|
| __Description__ | Specifies the SSL cipher suites that will be enabled for SSL connections. The property value is a whitespace separated list of tokens<br><br>Check "http://<ox-grizzly-hostname>:<ox-grizzly-port>/stats/diagnostic?param=ciphersuites" to check available cipher suites.<br><br>Default value is empty (fall-back to current JVM's default SSL cipher suite)<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | pop3.properties |

---
