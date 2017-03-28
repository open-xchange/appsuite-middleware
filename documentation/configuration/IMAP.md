---
title: IMAP
---

To differentiate between settings that apply all IMAP accounts or settings that only apply to the primary account
each property containing the "[.primary]" suffix allows to specify a value only applicable to the primary account
by appending the "primary." suffix to properties' common "com.openexchange.imap." prefix..
E.g.
"com.openexchange.imap.imapTimeout=50000" specifies 50sec read timeout for every IMAP account
"com.openexchange.imap.primary.imapTimeout=20000" specifies 20sec read timeout for primary-only IMAP account


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
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a> |
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
