---
title: Port
---

This page shows all properties with the tag: Port

| __Key__ | com.openexchange.connector.networkListenerPort |
|:----------------|:--------|
| __Description__ | The default port for the connector's http network listener<br> |
| __Default__ | 8009 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.connector.networkSslListenerPort |
|:----------------|:--------|
| __Description__ | The default port for the connector's https network listener<br> |
| __Default__ | 8010 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.spamhandler.spamassassin.port |
|:----------------|:--------|
| __Description__ | If the mail should be send to spamd specify the port of the spamassassin<br>daemon here<br> |
| __Default__ | 783 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.spamhandler.spamassassin.spamd |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spamassassin.html">Spamassassin</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | spamassassin.properties |

---
| __Key__ | com.openexchange.userfeedback.smtp.port |
|:----------------|:--------|
| __Description__ | The SMTP server port to connect to.<br> |
| __Default__ | 587 |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Feedback.html">Feedback</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | userfeedbackmail.properties |

---
| __Key__ | com.openexchange.noreply.port |
|:----------------|:--------|
| __Description__ | Specifies the SMTP server port for the no-reply account.<br> |
| __Default__ | 25 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/No-Reply.html">No-Reply</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | noreply.properties |

---
| __Key__ | com.openexchange.client.onboarding.caldav.url |
|:----------------|:--------|
| __Description__ | Specifies the URL to the CalDAV end-point; e.g. "dav.open-xchange.invalid" or "http://dav.open-xchange.invalid".<br><br>Note:<br>Specifying a protocol/scheme is optional and may be used to control whether the end-point is<br>supposed to be accessed via SSL or not.<br>Moreover, any path information is stripped off as only host name, port and SSL/No-SSL are relevant.<br>The administrator has ensure that end-point is reachable by a well-known path;<br>E.g. "PROPFIND /dav.example.com%3A8800/.well-known/caldav HTTP/1.1"<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CalDAV.html">CalDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | client-onboarding-caldav.properties |

---
| __Key__ | com.openexchange.client.onboarding.carddav.url |
|:----------------|:--------|
| __Description__ | Specifies the URL to the CardDAV end-point; e.g. "dav.open-xchange.invalid" or "http://dav.open-xchange.invalid".<br><br>Note:<br>Specifying a protocol/scheme is optional and may be used to control whether the end-point is<br>supposed to be accessed via SSL or not.<br>Moreover, any path information is stripped off as only host name, port and SSL/No-SSL are relevant.<br>The administrator has ensure that end-point is reachable by a well-known path;<br>E.g. "PROPFIND /dav.example.com%3A8843/.well-known/carddav HTTP/1.1"<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CardDAV.html">CardDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | client-onboarding-carddav.properties |

---
| __Key__ | com.openexchange.client.onboarding.eas.url |
|:----------------|:--------|
| __Description__ | Specifies the URL to the EAS end-point; e.g. "eas.open-xchange.invalid" or "http://eas.open-xchange.invalid".<br><br>Note:<br>Specifying a protocol/scheme is optional and may be used to control whether the end-point is<br>supposed to be accessed via SSL or not.<br>Moreover, any path information is stripped off as only host name, port and SSL/No-SSL are relevant.<br>The administrator has ensure that end-point is reachable by a well-known path.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/EAS.html">EAS</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | client-onboarding-eas.properties |

---
| __Key__ | com.openexchange.client.onboarding.mail.imap.port |
|:----------------|:--------|
| __Description__ | Specifies the IMAP port.<br>If not set, falls-back to internal settings for accessing the primary account.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | client-onboarding-mail.properties |

---
| __Key__ | com.openexchange.client.onboarding.mail.smtp.port |
|:----------------|:--------|
| __Description__ | Specifies the SMTP port.<br>If not set, falls-back to internal settings for accessing the primary account.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | client-onboarding-mail.properties |

---
| __Key__ | com.openexchange.report.client.proxy.port |
|:----------------|:--------|
| __Description__ | The port of the proxy, that should be used.<br> |
| __Default__ | 8080 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.report.client.proxy.address |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Report.html">Report</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Proxy.html">Proxy</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | reportclient.properties |

---
| __Key__ | com.openexchange.mail.filter.port |
|:----------------|:--------|
| __Description__ | Specifies the SIEVE port<br>-----------------------------------------------------------<br>NOTE: 2000 is the deprecated port number for SIEVE (now assigned to some Cisco SCCP protocol by the IANA)<br>      4190 is the new one used with most recent Linux and IMAP implementations.<br>Please check your system's default port defined at /etc/services.<br>-----------------------------------------------------------<br> |
| __Default__ | 4190 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.rmi.port |
|:----------------|:--------|
| __Description__ | Specifies the rmi port.<br> |
| __Default__ | 1099 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/RMI.html">RMI</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | rmi.properties |

---
| __Key__ | com.openexchange.mail.autoconfig.http.proxy |
|:----------------|:--------|
| __Description__ | Provides the possibility to specify a proxy that is used to access any HTTP end-points. If empty, no proxy is used.<br>Notation is: <optional-protocol> + "://" + <proxy-host> + ":" + <proxy-port><br>             With "http" as fall-back protocol<br>E.g. "67.177.104.230:58720" (using HTTP protocol) or "https://78.0.25.45:8345" (using HTTPS protocol)<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Autoconfig.html">Autoconfig</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | autoconfig.properties |

---
| __Key__ | com.openexchange.hazelcast.network.join.multicast.port |
|:----------------|:--------|
| __Description__ | Configures the multicast port used to discover other nodes in the cluster<br>dynamically. Only used if "com.openexchange.hazelcast.network.join" is set<br>to "multicast".<br> |
| __Default__ | 54327 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.hazelcast.network.join |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.network.port |
|:----------------|:--------|
| __Description__ | The port Hazelcast will listen for incoming connections.<br> |
| __Default__ | 5701 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.network.portAutoIncrement |
|:----------------|:--------|
| __Description__ | Configures if automatically the next port should be tried if the incoming<br>port is already in use.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.network.outboundPortDefinitions |
|:----------------|:--------|
| __Description__ | By default, Hazelcast lets the system to pick up an ephemeral port during<br>socket bind operation. But security policies/firewalls may require to<br>restrict outbound ports to be used by Hazelcast enabled applications. To<br>fulfill this requirement, you can configure Hazelcast to use only defined<br>outbound ports. You can use port ranges and/or comma separated ports, e.g.<br>"35000-35100" or "36001, 36002, 36003".<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.nosql.cassandra.port |
|:----------------|:--------|
| __Description__ | Defines the port on which the Cassandra server is running<br> |
| __Default__ | 9042 |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Cassandra.html">Cassandra</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | cassandra.properties |

---
| __Key__ | IMAP_PORT |
|:----------------|:--------|
| __Description__ | Port on which the IMAP server is listening.<br> |
| __Default__ | 143 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP_Auth.html">IMAP Auth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | imapauth.properties |

---
| __Key__ | com.openexchange.push.udp.registerPort |
|:----------------|:--------|
| __Description__ | Port where the clients send the push registration request to.<br> |
| __Default__ | 44335 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | push-udp.properties |

---
| __Key__ | com.openexchange.push.udp.multicastPort |
|:----------------|:--------|
| __Description__ | Specifies the multicast port.<br> |
| __Default__ | 9982 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | push-udp.properties |

---
| __Key__ | com.openexchange.filestore.sproxyd.[filestoreID].hosts |
|:----------------|:--------|
| __Description__ | Specifies the hosts as <hostname>:<port> pairs to be used for network communication.<br>At least one host must be provided, multiple hosts can be specified as comma-separated<br>list.<br>Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | filestore-sproxyd.properties |

---
| __Key__ | com.openexchange.twitter.http.proxyPort |
|:----------------|:--------|
| __Description__ | The HTTP proxy port.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.twitter.http.proxyHost |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Twitter.html">Twitter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | twitter.properties |

---
| __Key__ | mail.[protocol].port |
|:----------------|:--------|
| __Description__ | The port number of the mail server for the specified protocol. If not specified the protocol's default port number is used.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.port |
|:----------------|:--------|
| __Description__ | The SMTP server port to connect to, if the connect() method doesn't explicitly specify one.<br> |
| __Default__ | 25 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.port |
|:----------------|:--------|
| __Description__ | The IMAP server port to connect to, if the connect() method doesn't explicitly specify one.<br> |
| __Default__ | 143 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.localport |
|:----------------|:--------|
| __Description__ | Local port number to bind to when creating the IMAP socket. Defaults to the port number picked by the Socket class.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.port |
|:----------------|:--------|
| __Description__ | The POP3 server port to connect to, if the connect() method doesn't explicitly specify one.<br> |
| __Default__ | 110 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.localport |
|:----------------|:--------|
| __Description__ | Local port number to bind to when creating the POP3 socket. Defaults to the port number picked by the Socket class.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | javamail.properties |

---
| __Key__ | com.openexchange.custom.spamexperts.imapurl |
|:----------------|:--------|
| __Description__ | the imap url to the spamexperts imap server<br><br>"Example1:" imap://myserver.example.com<br>if the imap server offers STARTTLS, communication will be encrypted<br><br>"Example2:" imaps:myserver.example.com:993<br>connect to port 993 using a secure connection directly<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spamexperts.html">Spamexperts</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | spamexperts.properties |

---
| __Key__ | JMXPort |
|:----------------|:--------|
| __Description__ | Define the port for the RMI Registry.<br> |
| __Default__ | 9999 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Management.html">Management</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/RMI.html">RMI</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | management.properties |

---
| __Key__ | JMXServerPort |
|:----------------|:--------|
| __Description__ | Define the JMX RMI Connector Server port. Typically chosen randomly by JVM.<br>-1 means that the port is randomly determined by JVM.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Management.html">Management</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/RMI.html">RMI</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | management.properties |

---
