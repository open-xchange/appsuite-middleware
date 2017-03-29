---
title: Timeout
---

This page shows all properties with the tag: Timeout

| __Key__ | com.openexchange.html.css.parse.timeout |
|:----------------|:--------|
| __Description__ | Specify the amount of seconds to wait for a CSS content being parsed.<br>This property influences parsing of HTML messages. If CSS could not be parsed in time, CSS is stripped from message's content.<br> |
| __Default__ | 4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.imap[.primary].imapTimeout |
|:----------------|:--------|
| __Description__ | Define the socket read timeout value in milliseconds. A value less than<br>or equal to zero is infinite timeout. See also mail.imap.timeout<br> |
| __Default__ | 50000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].imapConnectionTimeout |
|:----------------|:--------|
| __Description__ | Define the socket connect timeout value in milliseconds. A value less than<br>or equal to zero is infinite timeout. See also mail.imap.connectiontimeout<br> |
| __Default__ | 20000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].connectionTimeout |
|:----------------|:--------|
| __Description__ | The connection timeout in milliseconds. If establishing a new HTTP connection to a certain<br>host, it is blacklisted until it is considered available again. A periodic heartbeat task<br>that tries to read the namespace configuration (<protocol>://<host>/<path>/.conf) decides<br>whether an endpoint is considered available again.<br> |
| __Default__ | 5000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].socketReadTimeout |
|:----------------|:--------|
| __Description__ | The socket read timeout in milliseconds. If waiting for the next expected TCP packet exceeds<br>this value, the host is blacklisted until it is considered available again. A periodic heartbeat<br>task that tries to read the namespace configuration (<protocol>://<host>/<path>/.conf) decides<br>whether an endpoint is considered available again.<br> |
| __Default__ | 15000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.mail.compose.share.preview.timeout |
|:----------------|:--------|
| __Description__ | Defines default timeout in milliseconds for preview image creation.<br> |
| __Default__ | 1000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | mail-compose.properties |

---
| __Key__ | com.openexchange.spamhandler.spamassassin.timeout |
|:----------------|:--------|
| __Description__ | If the mail should be send to spamd specify the timeout after which the<br>try to connect is aborted here<br> |
| __Default__ | 10 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.spamhandler.spamassassin.spamd |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spamassassin.html">Spamassassin</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | spamassassin.properties |

---
| __Key__ | com.openexchange.userfeedback.smtp.timeout |
|:----------------|:--------|
| __Description__ | Socket read timeout value in milliseconds.<br> |
| __Default__ | 50000 |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | userfeedbackmail.properties |

---
| __Key__ | com.openexchange.userfeedback.smtp.connectionTimeout |
|:----------------|:--------|
| __Description__ | Socket connection timeout value in milliseconds.<br> |
| __Default__ | 10000 |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | userfeedbackmail.properties |

---
| __Key__ | readProperty.9 |
|:----------------|:--------|
| __Description__ | A property of the db read connection. Should be kept at its default.<br> |
| __Default__ | connectTimeout=15000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | configdb.properties |

---
| __Key__ | readProperty.10 |
|:----------------|:--------|
| __Description__ | A property of the db read connection. Should be kept at its default.<br> |
| __Default__ | socketTimeout=15000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | configdb.properties |

---
| __Key__ | writeProperty.9 |
|:----------------|:--------|
| __Description__ | A property of the db write connection. Should be kept at its default.<br> |
| __Default__ | connectTimeout=15000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | configdb.properties |

---
| __Key__ | writeProperty.10 |
|:----------------|:--------|
| __Description__ | A property of the db write connection. Should be kept at its default.<br> |
| __Default__ | socketTimeout=15000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | configdb.properties |

---
| __Key__ | com.openexchange.http.grizzly.wsTimeoutMillis |
|:----------------|:--------|
| __Description__ | Specifies the Web Socket timeout in milliseconds<br> |
| __Default__ | 900000 |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.websockets.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Websockets.html">Websockets</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.http.grizzly.readTimeoutMillis |
|:----------------|:--------|
| __Description__ | Specifies the read timeout, in milliseconds. A timeout of zero is interpreted as an infinite timeout.<br> |
| __Default__ | 60000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.http.grizzly.writeTimeoutMillis |
|:----------------|:--------|
| __Description__ | Specifies the write timeout, in milliseconds. A timeout of zero is interpreted as an infinite timeout.<br> |
| __Default__ | 60000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.sun.jndi.ldap.connect.timeout |
|:----------------|:--------|
| __Description__ | Timeouts are useful to get quick responses for login requests. <br>This timeout is used if a new connection is established.<br> |
| __Default__ | 10000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | ldapauth.properties |

---
| __Key__ | com.sun.jndi.ldap.read.timeout |
|:----------------|:--------|
| __Description__ | This timeout only works since Java 6 SE to time out waiting for a response.<br> |
| __Default__ | 10000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | ldapauth.properties |

---
| __Key__ | com.openexchange.mail.filter.connectionTimeout |
|:----------------|:--------|
| __Description__ | Specifies when the connection should time out (value in milliseconds).<br> |
| __Default__ | 30000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.authTimeout |
|:----------------|:--------|
| __Description__ | Specifies when the connection should time out (value in milliseconds) when performing SASL authentication against Sieve end-point.<br> |
| __Default__ | 6000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.pop3.pop3Timeout |
|:----------------|:--------|
| __Description__ | Define the socket read timeout value in milliseconds. A value less than<br>or equal to zero is infinite timeout. See also mail.smtp.timeout<br>Default is 50000<br> |
| __Default__ | 50000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | pop3.properties |

---
| __Key__ | com.openexchange.pop3.pop3ConnectionTimeout |
|:----------------|:--------|
| __Description__ | Define the socket connect timeout value in milliseconds. A value less<br>or equal to zero is infinite timeout. See also mail.smtp.connectiontimeout<br>Default is 20000<br> |
| __Default__ | 20000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | pop3.properties |

---
| __Key__ | com.openexchange.hazelcast.maxOperationTimeout |
|:----------------|:--------|
| __Description__ | Specifies the implicit maximum operation timeout in milliseconds for<br>operations on distributed data structures, if no explicit timeout is<br>specified for an operation.<br> |
| __Default__ | 30000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.smtp[.primary].smtpTimeout |
|:----------------|:--------|
| __Description__ | Define the socket read timeout value in milliseconds. A value less than<br>or equal to zero is infinite timeout. See also mail.smtp.timeout<br>Default is 50000<br> |
| __Default__ | 50000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.smtp[.primary].smtpConnectionTimeout |
|:----------------|:--------|
| __Description__ | Define the socket connect timeout value in milliseconds. A value less<br>or equal to zero is infinite timeout. See also mail.smtp.connectiontimeout<br>Default is 10000<br> |
| __Default__ | 10000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.nosql.cassandra.connectTimeout |
|:----------------|:--------|
| __Description__ | Specifies the connect timeout in milliseconds<br> |
| __Default__ | 5000 |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Cassandra.html">Cassandra</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | cassandra.properties |

---
| __Key__ | IMAP_TIMEOUT |
|:----------------|:--------|
| __Description__ | Socket I/O timeout value in milliseconds.<br> |
| __Default__ | 5000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP_Auth.html">IMAP Auth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | imapauth.properties |

---
| __Key__ | IMAP_CONNECTIONTIMEOUT |
|:----------------|:--------|
| __Description__ | Socket connection timeout value in milliseconds.<br> |
| __Default__ | 5000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP_Auth.html">IMAP Auth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | imapauth.properties |

---
| __Key__ | com.openexchange.push.udp.registerTimeout |
|:----------------|:--------|
| __Description__ | Time in milliseconds a client registration is kept.<br> |
| __Default__ | 3600000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | push-udp.properties |

---
| __Key__ | com.openexchange.filestore.sproxyd.[filestoreID].connectionTimeout |
|:----------------|:--------|
| __Description__ | The connection timeout in milliseconds. If establishing a new HTTP connection to a certain<br>host, it is blacklisted until it is considered available again. A periodic heartbeat task<br>that tries to read the namespace configuration (<protocol>://<host>/<path>/.conf) decides<br>whether an endpoint is considered available again.<br> |
| __Default__ | 5000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | filestore-sproxyd.properties |

---
| __Key__ | com.openexchange.filestore.sproxyd.[filestoreID].socketReadTimeout |
|:----------------|:--------|
| __Description__ | The socket read timeout in milliseconds. If waiting for the next expected TCP packet exceeds<br>this value, the host is blacklisted until it is considered available again. A periodic heartbeat<br>task that tries to read the namespace configuration (<protocol>://<host>/<path>/.conf) decides<br>whether an endpoint is considered available again.<br> |
| __Default__ | 15000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | filestore-sproxyd.properties |

---
| __Key__ | com.openexchange.twitter.http.connectionTimeout |
|:----------------|:--------|
| __Description__ | Connection time out<br> |
| __Default__ | 20000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Twitter.html">Twitter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | twitter.properties |

---
| __Key__ | com.openexchange.twitter.http.readTimeout |
|:----------------|:--------|
| __Description__ | Read time out<br> |
| __Default__ | 120000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Twitter.html">Twitter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | twitter.properties |

---
| __Key__ | mail.smtp.connectiontimeout |
|:----------------|:--------|
| __Description__ | Socket connection timeout value in milliseconds.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.timeout |
|:----------------|:--------|
| __Description__ | Socket I/O timeout value in milliseconds.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.connectiontimeout |
|:----------------|:--------|
| __Description__ | Socket connection timeout value in milliseconds.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.timeout |
|:----------------|:--------|
| __Description__ | Socket I/O timeout value in milliseconds.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
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
| __Key__ | mail.imap.connectionpooltimeout |
|:----------------|:--------|
| __Description__ | Timeout value in milliseconds for connection pool connections.<br> |
| __Default__ | 45000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.connectiontimeout |
|:----------------|:--------|
| __Description__ | Socket connection timeout value in milliseconds.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.timeout |
|:----------------|:--------|
| __Description__ | Socket I/O timeout value in milliseconds.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
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
| __Key__ | serviceUsageTimeout |
|:----------------|:--------|
| __Description__ | The service usage timeout in milliseconds. This property only has<br>effect if property 'serviceUsageInspection' is set to 'true'.<br> |
| __Default__ | 10000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/System.html">System</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | system.properties |

---
