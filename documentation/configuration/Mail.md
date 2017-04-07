---
title: Mail
---

| __Key__ | com.openexchange.mail.loginSource |
|:----------------|:--------|
| __Description__ | Set the login source for primary mail/transport account; meaning which source is taken to determine a user's<br>login for mailing system. If 'login' is set, then user's individual mail login<br>as defined in user storage is taken. If 'mail' is set, then user's individual<br>primary email address is taken. If 'name' is set, then user's individual system's<br>user name is taken.<br>Currently known values: login, mail, and name<br> |
| __Default__ | login |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.passwordSource |
|:----------------|:--------|
| __Description__ | Set the password source for primary mail/transport account; meaning which source is taken to determine a user's<br>password to login into mailing system. If 'session' is set, then user's individual<br>system's password is taken. If 'global' is set, then the value specified through<br>property 'com.openexchange.mail.masterPassword' is taken.<br>Currently known values: session and global<br> |
| __Default__ | session |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.mailServerSource |
|:----------------|:--------|
| __Description__ | Set the mail server source for primary mail account; meaning which source is taken to determine the mail<br>server into which the user wants to login to access mails. Set to 'global' to take<br>the value specified through property "com.openexchange.mail.mailServer". Set to<br>'user' to take user's individual mail server settings as specified in storage.<br>Currently known values: user and global<br> |
| __Default__ | user |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.transportServerSource |
|:----------------|:--------|
| __Description__ | Set the transport server source for primary mail account; meaning which source is taken to determine the transport<br>server into which the user wants to login to transport mails. Set to 'global' to take<br>the value specified through property "com.openexchange.mail.transportServer". Set to<br>'user' to take user's individual transport server settings as specified in storage.<br>Currently known values: user and global<br> |
| __Default__ | user |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Transport.html">Transport</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.masterPassword |
|:----------------|:--------|
| __Description__ | The master password for primary mail/transport server. Only takes effect when property<br>"com.openexchange.mail.passwordSource" is set to "global"<br> |
| __Default__ | secret |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.mailServer |
|:----------------|:--------|
| __Description__ | Primary mail server: e.g. 192.168.178.32:8143 or imap://192.168.178.32:7143<br>Only takes effect when property "com.openexchange.mail.mailServerSource" is set to "global"<br> |
| __Default__ | imap://localhost |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.transportServer |
|:----------------|:--------|
| __Description__ | Primary transport server: e.g. 192.168.178.32:125 or smtp://192.168.178.32:225<br>Only takes effect when property "com.openexchange.mail.transportServerSource" is set to "global"<br> |
| __Default__ | 127.0.0.1 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Transport.html">Transport</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.mailStartTls |
|:----------------|:--------|
| __Description__ | Set if STARTTLS should be used when connecting to the primary mail server<br>Only takes effect when property "com.openexchange.mail.mailServerSource" is set to "global"<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.transportStartTls |
|:----------------|:--------|
| __Description__ | Set if STARTTLS should be used when connecting to the primary transport server<br>Only takes effect when property "com.openexchange.mail.transportServerSource" is set to "global" <br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Transport.html">Transport</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.defaultMailProvider |
|:----------------|:--------|
| __Description__ | The mail provider fallback if an URL does not contain/define a protocol<br> |
| __Default__ | imap |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | mail.mime.charset |
|:----------------|:--------|
| __Description__ | Define the default MIME charset used for character encoding. This setting will then be<br>accessible through system property "mail.mime.charset". This parameter takes<br>effect for the complete mail module where no charset is given.<br> |
| __Default__ | UTF-8 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.saneLogin |
|:----------------|:--------|
| __Description__ | Controls whether a login string is supposed to be converted to its ACE representation in case it is an E-Mail address;<br>e.g. "someone@m&uuml;ller.de" is converted to "someone@xn--mller-kva.de"<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.replaceWithComma |
|:----------------|:--------|
| __Description__ | Controls a possibly semi-colon-separated address list should be converted to comma-separated one<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.mailFetchLimit |
|:----------------|:--------|
| __Description__ | Define the max. fetch limit; meaning all mails whose count is less than or equal to<br>this value are going to be fetched with all attributes set. Thus these mails can be<br>put into message cache for subsequent list requests.<br> |
| __Default__ | 1000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.bodyDisplaySizeLimit |
|:----------------|:--------|
| __Description__ | Specifies the maximum size for message bodies that are allowed to be displayed.<br>Default is 10485760 (10 MB)<br> |
| __Default__ | 10485760 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.attachmentDisplaySizeLimit |
|:----------------|:--------|
| __Description__ | Specifies the maximum size (in bytes) for email text attachments that will be displayed inline<br>Default is 8192 (8 KB)<br> |
| __Default__ | 8192 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.mailAccessCacheShrinkerSeconds |
|:----------------|:--------|
| __Description__ | Define the interval seconds of the mail access cache's shrinker thread<br> |
| __Default__ | 3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Thread.html">Thread</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.mailAccessCacheIdleSeconds |
|:----------------|:--------|
| __Description__ | Define the idle seconds a mail access may reside in mail access cache before it is removed by shrinker thread.<br> |
| __Default__ | 4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Thread.html">Thread</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.quoteLineColors |
|:----------------|:--------|
| __Description__ | Comma-separated hex values of colors for displaying quoted text emails<br>Only used by OX6 UI<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.forwardUnquoted |
|:----------------|:--------|
| __Description__ | Define if forwarded text is supposed to be quoted<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.userFlagsEnabled |
|:----------------|:--------|
| __Description__ | Enable/disable user defined flags<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.allowNestedDefaultFolderOnAltNamespace |
|:----------------|:--------|
| __Description__ | This property defines if the default folders of an user (Draft, Sent, Spam<br>& Trash) are going to be placed right below folder "INBOX" even if<br>feature "altNamespace" is enabled. NOTE: This property requires that<br>subfolders are still permitted below initial folder "INBOX" even though<br>"altNamespace" is enabled.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.ignoreSubscription |
|:----------------|:--------|
| __Description__ | Defines if folder subscription is ignored when listing mail folders<br>If set to 'true', all folders - regardless of their subscription status - are<br>going to be listed<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.supportSubscription |
|:----------------|:--------|
| __Description__ | Define if underlying store should support subscription<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.defaultSeparator |
|:----------------|:--------|
| __Description__ | Define the separator within folder full names if not available from mail server<br> |
| __Default__ | / |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.watcherEnabled |
|:----------------|:--------|
| __Description__ | The watcher checks after watcherFrequency for mail connections used for<br>more than watcherTime milliseconds and logs this mail connection. If<br>watcherShallClose is set to true those connections will be closed.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.watcherFrequency |
|:----------------|:--------|
| __Description__ | Define watcher's frequency in milliseconds<br> |
| __Default__ | 10000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.watcherTime |
|:----------------|:--------|
| __Description__ | Define exceeding time in milliseconds for mail connections. If use time<br>of an mail connection exceeds this value it is logged. Thus unclosed<br>connections can be detected.<br> |
| __Default__ | 60000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.watcherShallClose |
|:----------------|:--------|
| __Description__ | Define if watcher is allowed to close exceeded mail connections<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.JavaMailProperties |
|:----------------|:--------|
| __Description__ | JavaMail Properties<br> |
| __Default__ | javamail.properties |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.phishingHeader |
|:----------------|:--------|
| __Description__ | A comma-separated list of headers which identifies phishing headers; e.g.<br>X-Phishing1,X-Phishing2,etc.<br>Leave empty for no phishing header.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.adminMailLoginEnabled |
|:----------------|:--------|
| __Description__ | Define whether a context admin is allowed to login to mail system or not.<br>Note that a mail account is supposed to exist if set to true; if not an<br>authentication error will occur.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.addClientIPAddress |
|:----------------|:--------|
| __Description__ | Set whether client's IP address should be added to mail headers on delivery<br>as custom header "X-Originating-IP"<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.clientIPAddressPattern |
|:----------------|:--------|
| __Description__ | Specifies the regular expression to use to extract the host name/IP address information out of the greeting string advertised by primary<br>IMAP server. Only applicable for primary IMAP server! Default is empty.<br><br>The regular expression is supposed to be specified in Java notation: http://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html<br><br>Moreover, either the complete regex is considered or in case a capturing group is present that group will be preferred.<br>I.e. "Dovecot at ([0-9a-zA-Z._-]\*) is ready", then the capturing group is supposed to extract the host name/IP address information<br> |
| __Default__ | false |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.rateLimitPrimaryOnly |
|:----------------|:--------|
| __Description__ | Define if the rateLimit and maxToCcBcc settings below will only affect<br>the primary account or all accounts<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.rateLimit |
|:----------------|:--------|
| __Description__ | Define the time (in milliseconds) which must pass by before a new mail can be sent<br>A value of 0 disables the limit.<br> |
| __Default__ | 0 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.rateLimitDisabledRange |
|:----------------|:--------|
| __Description__ | Define the comma-separated IP ranges for which a rate limit will not be applied.<br>Default is empty<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.maxToCcBcc |
|:----------------|:--------|
| __Description__ | Define the allowed maximum number of recipients in a mail<br>A value of 0 disables the limit.<br> |
| __Default__ | 0 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.hidePOP3StorageFolders |
|:----------------|:--------|
| __Description__ | Whether folders which carry a POP3 account shall not be displayed.<br>This property affects primary account only.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.deleteDraftOnTransport |
|:----------------|:--------|
| __Description__ | Whether to delete draft messages when sent out<br>Note: Client MUST provide appropriate value in "sendtype" field;<br>see http://oxpedia.org/wiki/index.php?title=HTTP_API#Send_a_mail<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.supportMsisdnAddresses |
|:----------------|:--------|
| __Description__ | Define if MSISDN addresses are supported or not.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.translateDefaultFolders |
|:----------------|:--------|
| __Description__ | If set to 'false', translation of names of the mail default folders is not performed,<br>if naming differs from pre-defined default values. Thus custom set names can be specified<br>for Trash, Drafts, Sent & Spam folder.<br>By default this value is 'true'.<br>Pre-defined names are:<br>"Trash"<br>"Drafts"<br>"Sent objects"<br>"Spam"<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.authProxyDelimiter |
|:----------------|:--------|
| __Description__ | Define a delimiter to be used to seperate Proxy from Userlogin<br>If defined, a proxy user can login on behalf of a user using the form<br><PROXYACCOUNT><DELIMITER><USERACCOUNT><br>NOTE: The underlying Mailserver must support SASL AUTHPROXYING<br>The open-xchange mailfilterbundle does not support it, so it will raise errors<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.maxMailSize |
|:----------------|:--------|
| __Description__ | Specifies the max. mail size allowed being transported<br>A value of zero or less means infinite.<br>Default is -1 (infinite)<br> |
| __Default__ | -1 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.hideDetailsForDefaultAccount |
|:----------------|:--------|
| __Description__ | Whether to hide rather technical data from JSON representation of the primary mail account<br>e.g. port, server name, secure flag, etc.<br>Default is false<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.maxForwardCount |
|:----------------|:--------|
| __Description__ | Specifies the max. number of message attachments that are allowed to be forwarded as attachment<br>Default is 8<br> |
| __Default__ | 8 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.account.blacklist |
|:----------------|:--------|
| __Description__ | Specifies a black-list for those hosts that are covered by denoted IP range; e.g. "127.0.0.1-127.255.255.255, localhost, internal.domain.org"<br>Creation of mail accounts with this hosts will be prevented. Also the validation of those accounts will fail.<br>An empty value means no black-listing is active<br>Default is "127.0.0.1-127.255.255.255,localhost"<br> |
| __Default__ | 127.0.0.1-127.255.255.255,localhost |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Account.html">Mail Account</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.account.whitelist.ports |
|:----------------|:--------|
| __Description__ | Specifies a white-list for such ports that are allowed to connect against when setting up/validating an external mail account<br>An empty value means no white-listing is active<br>Default is: 143,993, 25,465,587, 110,995<br> |
| __Default__ | 143,993, 25,465,587, 110,995 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Account.html">Mail Account</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/White_List.html">White List</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.imageHost |
|:----------------|:--------|
| __Description__ | Specifies the host/domain from which to load inlined images contained in message content<br>Example "com.openexchange.mail.imageHost=http://my.imagehost.org".<br>In case no protocol/schema is specified, "http" is assumed by default<br>Default is empty; meaning to load from originating host<br>Exemplary setup:<br>- Artificially add a host name to /etc/hosts:<br>  127.0.0.1     imageserver.open-xchange.com<br>  - Enable the "com.openexchange.mail.imageHost" property in mail.properties:<br>    com.openexchange.mail.imageHost=http://imageserver.open-xchange.com<br>    - Check a mail with an inline image<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.signature.maxImageSize |
|:----------------|:--------|
| __Description__ | Specifies the maximum size (in MB) for one image contained in the HTML mail signature<br> |
| __Default__ | 1 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Signature.html">Signature</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.signature.maxImageLimit |
|:----------------|:--------|
| __Description__ | Specified the maximum amount of images that are allowed in a single HTML mail signature<br> |
| __Default__ | 3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Signature.html">Signature</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.useStaticDefaultFolders |
|:----------------|:--------|
| __Description__ | Defines whether standard folder names should be initialized with the default values or not. <br>The default values can be configured within the Adminuser.properties file.<br> |
| __Default__ | false |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.imap.setSpecialUseFlags |
|:----------------|:--------|
| __Description__ | Defines whether the ox middleware is allowed to set special use flags.<br>If set to 'false' the ox middleware will never set any special use flags on folders on the imap server.<br>If set to 'true' the ox middleware will only set special use flags if no special use flag of that type exist yet.<br> |
| __Default__ | false |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.mail.preferSentDate |
|:----------------|:--------|
| __Description__ | Specifies what to consider as the date of a mail; either the internal received date or mail's sent date (as given by "Date" header).<br>This property is considered in case a client passes special "date" (661) column to "columns" parameter and/or "sort" parameter.<br> |
| __Default__ | false |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.maxDriveAttachments |
|:----------------|:--------|
| __Description__ | Specifies the max. number of Drive documents that are allowed to be sent via E-Mail<br> |
| __Default__ | 20 |
| __Version__ | 7.6.2 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.authType |
|:----------------|:--------|
| __Description__ | Specifies the authentication type which should be used for primary account's mail access. Known values: 'login', 'xoauth2', and "oauthbearer"<br> |
| __Default__ | login |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/OAuth.html">OAuth</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.transport.authType |
|:----------------|:--------|
| __Description__ | Specifies the authentication type which should be used for primary account's mail transport. Known values: 'login', 'xoauth2', and "oauthbearer"<br> |
| __Default__ | login |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/OAuth.html">OAuth</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.flagging.mode |
|:----------------|:--------|
| __Description__ | Specifies how color labels and special \Flagged system flag are connected (or not). Possible values:<br>-<code>colorOnly</code> Only color flags are available. The special \Flagged system flag is not touched.<br>-<code>flaggedOnly</code> Only special \Flagged system flag is used. Color labels are not published.<br>-<code>flaggedAndColor</code> Both - color flags and special \Flagged system flag - are available and set independently.<br>-<code>flaggedImplicit</code>Both - color flags and special \Flagged system flag - are available. A certain color label is linked with the \Flagged system flag. That is to add a color to colorless flagged mails and to add flagged to unflagged but colored mails.<br> |
| __Default__ | colorOnly |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mail.flagging.color |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.flagging.color |
|:----------------|:--------|
| __Description__ | Specifies the color which should be added to colorless flagged mails in case the flagging mode is "flaggedImplicit". Only values from 1 to 10 are allowed.<br> |
| __Default__ | 1 |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mail.flagging.mode |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.maliciousFolders.enabled |
|:----------------|:--------|
| __Description__ | Enables/disables support for malicious folders.<br><br>If enabled and a mail is fetched from a folder contained in listing configured through "com.openexchange.mail.maliciousFolders.listing" property, the mail's JSON representation contains an additional "malicious: true" field and possible HTML content is processed in the way to disable any hyper-links.<br><br>I.e.<br>"...&lt;a href="http://evil.com/click.me"&gt;Get something for free here&lt;/a&gt;..."<br>is turned to<br>"...&lt;a href="#" onclick"return false;" data-disabled="true"&gt;Get something for free here&lt;/a&gt;..."<br> |
| __Default__ | true |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mail.maliciousFolders.listing |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.maliciousFolders.listing |
|:----------------|:--------|
| __Description__ | Specifies the full paths for such mail folders in the primary account's folder tree that are supposed being considered as malicious and therefore a special treatment happens; e.g. hyper-links that occur in mail content are not displayed and/or are not clickable.<br><br>The value is supposed to be comma-separated list of folder paths. An entry in the CSV list is either a full path of a folder in the primary mail account (e.g. "INBOX/Malware") or a reserved identifier denoting the standard folder and its sub-folders of every mail account (incl. Unified Mail). Supported reserved identifiers are: "$Spam", "$Drafts", "$Sent", "$Trash", "$Confirmed-spam", "$Confirmed-ham".<br><br>Example: $Spam, INBOX/Malware<br>Special treatment happens for standard Spam folder and for the "INBOX/Malware" folder in primary account's folder tree hierarchy.<br> |
| __Default__ | $Spam, $Confirmed-spam |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mail.maliciousFolders.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexhange.mail.authType |
|:----------------|:--------|
| __Description__ | Specifies the authentication type which should be used for mail access of the primary account. Known values: 'login', 'xoauth2', and "oauthbearer"<br> |
| __Default__ | login |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexhange.mail.transport.authType |
|:----------------|:--------|
| __Description__ | Specifies the authentication type which should be used for mail transport of the primary account. Known values: 'login', 'xoauth2', and "oauthbearer"<br> |
| __Default__ | login |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mailmapping.lookUpByDomain |
|:----------------|:--------|
| __Description__ | This option specifies how look-up of mail addresses is performed.<br><br>Setting this option to "true" means that the domain part of a mail address (the part after the "@" sign) is used to find a matching<br>context by checking the login mappings. That mechanism does only work if Open-Xchange setup strictly defines a dedicated and unique domain<br>per context. Otherwise that look-up mechanism will lead to wrong results.<br><br>Setting this option to "false" means that the mail address is going to be looked-up on a per database schema basis. For each known schema<br>a query is performed to check whether there is such an internal user.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mailresolver.properties |

---
