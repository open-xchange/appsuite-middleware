---
title: Javamail properties
---

This file contains all known JavaMail properties structured by sections\:
JavaMail, JavaMail API System Properties, SMTP, IMAP, POP3 & Internet
All configurations done in this file will overwrite the ones set and used before, so modify it with care!


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
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.charset |
|:----------------|:--------|
| __Description__ | The default charset to be used by JavaMail. If not set (the normal case), the standard J2SE file.encoding System property is used.<br>This allows applications to specify a default character set for sending messages that's different than the character set used for files stored on the system.<br>This is common on Japanese systems.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.decodetext.strict |
|:----------------|:--------|
| __Description__ | RFC 2047 requires that encoded text start at the beginning of a whitespace separated word.<br>Some mailers, especially Japanese mailers, improperly encode text and include encoded text in the middle of words.<br>This property controls whether JavaMail will attempt to decode such incorrectly encoded text.<br>The default is true, meaning that JavaMail wil not attempt to decode such improperly decoded text.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.encodeeol.strict |
|:----------------|:--------|
| __Description__ | When choosing an encoding for the data of a message, JavaMail assumes that any of CR, LF, or CRLF are valid line<br>terminators in message parts that contain only printable ASCII characters, even if the part is not a MIME text type.<br>It's common, especially on UNIX systems, for data of MIME type application/octet-stream (for example) to really be<br>textual data that should be transmitted with the encoding rules for MIME text. In rare cases, such pure ASCII text<br>may in fact be binary data in which the CR and LF characters must be preserved exactly.<br>If this property is set to true, JavaMail will consider a lone CR or LF in a body part that's not a MIME text type to<br>indicate that the body part needs to be encoded.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.user |
|:----------------|:--------|
| __Description__ | Default user name for SMTP<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.user |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.host |
|:----------------|:--------|
| __Description__ | The SMTP server to connect to.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.port |
|:----------------|:--------|
| __Description__ | The SMTP server port to connect to, if the connect() method doesn't explicitly specify one.<br> |
| __Default__ | 25 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.connectiontimeout |
|:----------------|:--------|
| __Description__ | Socket connection timeout value in milliseconds.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.timeout |
|:----------------|:--------|
| __Description__ | Socket I/O timeout value in milliseconds.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.from |
|:----------------|:--------|
| __Description__ | Email address to use for SMTP MAIL command. This sets the envelope return address.<br> Defaults to msg.getFrom() or InternetAddress.getLocalAddress(). NOTE: mail.smtp.user was previously used for this. <br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.smtp.user |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.localhost |
|:----------------|:--------|
| __Description__ | Local host name. Defaults to InetAddress.getLocalHost().getHostName().<br>Should not normally need to be set if your JDK and your name service are configured properly.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.ehlo |
|:----------------|:--------|
| __Description__ | If false, do not attempt to sign on with the EHLO command. Normally failure of the EHLO command will<br>fallback to the HELO command; this property exists only for servers that don't fail EHLO properly or<br>don't implement EHLO properly.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.auth |
|:----------------|:--------|
| __Description__ | If true, attempt to authenticate the user using the AUTH command.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.dsn.notify |
|:----------------|:--------|
| __Description__ | The NOTIFY option to the RCPT command. Either NEVER, or some combination of SUCCESS, FAILURE, and DELAY (separated by commas).<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.dns.ret |
|:----------------|:--------|
| __Description__ | The RET option to the MAIL command. Either FULL or HDRS.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.allow8bitmime |
|:----------------|:--------|
| __Description__ | If set to true, and the server supports the 8BITMIME extension, text parts of messages that use the "quoted-printable" or "base64"<br>encodings are converted to use "8bit" encoding if they follow the RFC2045 rules for 8bit text. <br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.sendpartial |
|:----------------|:--------|
| __Description__ | If set to true, and a message has some valid and some invalid addresses, send the message anyway, reporting the partial failure with<br>a SendFailedException. If set to false , the message is not sent to any of the recipients if there is an invalid recipient<br>address.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.saslrealm |
|:----------------|:--------|
| __Description__ | The realm to use with DIGEST-MD5 authentication.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.quitwait |
|:----------------|:--------|
| __Description__ | If set to true, causes the transport to wait for the response to the QUIT command.<br>If set to false, the QUIT command is sent and the connection is immediately closed.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.reportsuccess |
|:----------------|:--------|
| __Description__ | If set to true, causes the transport to include an SMTPAddressSucceededException for each address that is successful.<br>Note also that this will cause a SendFailedException to be thrown from the sendMessage method of SMTPTransport even if all addresses were<br>correct and the message was sent successfully.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.mailextension |
|:----------------|:--------|
| __Description__ | Extension string to append to the MAIL command. The extension string can be used to specify standard SMTP service extensions as well as vendor-specific extensions.<br>Typically the application should use the SMTPTransport method supportsExtension to verify that the server supports the desired service extension.<br>See RFC 1869 and other RFCs that define specific extensions.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.userset |
|:----------------|:--------|
| __Description__ | If set to true, use the RSET command instead of the NOOP command in the isConnected method.<br>In some cases sendmail will respond slowly after many NOOP commands; use of RSET avoids this sendmail issue.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
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
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
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
| __Key__ | mail.pop3.user |
|:----------------|:--------|
| __Description__ | Default user name for POP3.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.user |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
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
| __Description__ | If set to true, the POP3 TOP command will not be used to fetch message headers. This is useful for POP3 servers that don't properly implement the TOP command,<br>or that provide incorrect information in the TOP command results.<br> |
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
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
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
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.store.maildir.maxnumofheadercaches |
|:----------------|:--------|
| __Description__ | Define the maximum number of maildir folder header caches.<br> |
| __Default__ | 10 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.store.maildir.headercacheexpire |
|:----------------|:--------|
| __Description__ | Define the timeout for header caches (in milliseconds).<br> |
| __Default__ | 3600000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
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
