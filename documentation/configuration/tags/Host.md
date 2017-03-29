---
title: Host
---

This page shows all properties with the tag: Host

| __Key__ | com.openexchange.jump.endpoint.[system-name] |
|:----------------|:--------|
| __Description__ | Specifies a list jump end-points.<br>[system-name] must be replaced with the name of the system. <br>E.g. com.openexchange.jump.endpoint.mysystem=http://my.host.invalid/identities?action=receiveIdentity<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Jump.html">Jump</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | tokens.properties |

---
| __Key__ | com.openexchange.connector.networkListenerHost |
|:----------------|:--------|
| __Description__ | The host for the connector's http network listener. Set to "\*" if you<br>want to listen on all available interfaces.<br>"Default value:" 127.0.0.1, bind to localhost only.<br> |
| __Default__ | 127.0.0.1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.dovecot.doveadm.endpoints |
|:----------------|:--------|
| __Description__ | Specifies the URIs to the Dovecot DoveAdm REST interface end-points. <br>e.g. "http://dovecot1.host.invalid:8081, http://dovecot2.host.invalid:8081, http://dovecot3.host.invalid:8081"<br><br>Moreover connection-related attributes are allowed to be specified to influence HTTP connection and pooling behavior<br>com.openexchange.dovecot.doveadm.endpoints.totalConnections        The number of total connections held in HTTP connection pool<br>com.openexchange.dovecot.doveadm.endpoints.maxConnectionsPerRoute  The number of connections per route held in HTTP connection pool; or less than/equal to 0 (zero) for auto-determining<br>com.openexchange.dovecot.doveadm.endpoints.readTimeout             The read time-out in milliseconds (default is 10sec)<br>com.openexchange.dovecot.doveadm.endpoints.connectTimeout          The connect time-out in milliseconds (default is 3sec)<br>com.openexchange.dovecot.doveadm.endpoints.checkInterval           The time interval in milliseconds when to check if a previously black-listed end-point is re-available again (default is 60sec)<br><br>"Full example :"<br>com.openexchange.dovecot.doveadm.endpoints=http://dovecot1.host.invalid:8081, http://dovecot2.host.invalid:8081<br>com.openexchange.dovecot.doveadm.endpoints.totalConnections=100<br>com.openexchange.dovecot.doveadm.endpoints.maxConnectionsPerRoute=0 (max. connections per route is then determined automatically by specified end-points)<br>com.openexchange.dovecot.doveadm.endpoints.readTimeout=10000<br>com.openexchange.dovecot.doveadm.endpoints.connectTimeout=3000<br>com.openexchange.dovecot.doveadm.endpoints.checkInterval=60000<br><br>The values can be configured within a dedicated .properties file; e.g. 'doveadm.properties'.<br> |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/REST.html">REST</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | doveadm.properties |

---
| __Key__ | com.openexchange.imap[.primary].propagateHostNames |
|:----------------|:--------|
| __Description__ | Configure a comma-separated list of external IMAP server's host names which should receive client's IP address by a NOOP command, too<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | imap.properties |

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
| __Key__ | com.openexchange.mail.imageHost |
|:----------------|:--------|
| __Description__ | Specifies the host/domain from which to load inlined images contained in message content<br>Example "com.openexchange.mail.imageHost=http://my.imagehost.org".<br>In case no protocol/schema is specified, "http" is assumed by default<br>Default is empty; meaning to load from originating host<br>Exemplary setup:<br>- Artificially add a host name to /etc/hosts:<br>  127.0.0.1     imageserver.open-xchange.com<br>  - Enable the "com.openexchange.mail.imageHost" property in mail.properties:<br>    com.openexchange.mail.imageHost=http://imageserver.open-xchange.com<br>    - Check a mail with an inline image<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].hosts |
|:----------------|:--------|
| __Description__ | Specifies the API end-point pairs to be used. At least one host must be provided.<br>Multiple hosts can be specified as comma-separated list; e.g. "my1.clouddrive.invalid, my2.clouddrive.invalid"<br>Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | object_link |
|:----------------|:--------|
| __Description__ | Direct links for notifications are generated according to the following pattern.<br>[hostname] needs to be replaced with the hostname of your machine. This is done automatically by software on backend machines using the<br>hosts canonical host name.<br>[uiwebpath] is replaced with the value of com.openexchange.UIWebPath defined in server.properties.<br>[module], [object] and [folder] are replaced with the relevant IDs to generate the direct link.<br> |
| __Default__ | http://[hostname]/[uiwebpath]#!!&app=io.ox/[module]&id=[object]&folder=[folder] |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Notification.html">Notification</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Task.html">Task</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | notification.properties |

---
| __Key__ | com.openexchange.share.guestHostname |
|:----------------|:--------|
| __Description__ | Configures a separate hostname to use for guest users. This hostname is used <br>when generating external share links, as well as at other locations where <br>hyperlinks are constructed in the context of guest users.<br>Usually, the guest hostname refers to a separate subdomain of the <br>installation like "share.example.com", and is defined as an additional named<br>virtual host pointing to the web client's document root in the webserver's <br>configuration.<br>This property may defined statically here, overridden via config cascade, or<br>be provided through an additionally installed hostname service.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | share.properties |

---
| __Key__ | com.openexchange.spamhandler.spamassassin.hostname |
|:----------------|:--------|
| __Description__ | If the mail should be send to spamd specify the hostname of the spamassassin daemon here<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.spamhandler.spamassassin.spamd |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spamassassin.html">Spamassassin</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | spamassassin.properties |

---
| __Key__ | com.openexchange.userfeedback.smtp.hostname |
|:----------------|:--------|
| __Description__ | The SMTP server to connect to<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | userfeedbackmail.properties |

---
| __Key__ | com.openexchange.authentication.ucs.ldapUrl |
|:----------------|:--------|
| __Description__ | The ldap url; use ldaps:// for ssl.<br> |
| __Default__ | ldap://localhost |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Univention.html">Univention</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | authplugin.properties |

---
| __Key__ | readUrl |
|:----------------|:--------|
| __Description__ | The readURL holds the database host and the used schema name.<br>The read connection must point to the database slave.<br> |
| __Default__ | jdbc:mysql://localhost/configdb |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | configdb.properties |

---
| __Key__ | readUrl |
|:----------------|:--------|
| __Description__ | The writeURL holds the database host and the used schema name.<br>The write connection must point to the database master.<br> |
| __Default__ | jdbc:mysql://localhost/configdb |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | configdb.properties |

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
| __Key__ | com.openexchange.client.onboarding.mail.imap.host |
|:----------------|:--------|
| __Description__ | Specifies the IMAP host name.<br>If not set, falls-back to internal settings for accessing the primary account.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | client-onboarding-mail.properties |

---
| __Key__ | com.openexchange.client.onboarding.mail.smtp.host |
|:----------------|:--------|
| __Description__ | Specifies the SMTP host name.<br>If not set, falls-back to internal settings for accessing the primary account.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | client-onboarding-mail.properties |

---
| __Key__ | com.openexchange.mail.filter.server |
|:----------------|:--------|
| __Description__ | Specifies a default value for the sieve server.<br> |
| __Default__ | localhost |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.rmi.host |
|:----------------|:--------|
| __Description__ | Specifies the rmi host. Set this to 0 to bind on all interfaces.<br> |
| __Default__ | localhost |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/RMI.html">RMI</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | rmi.properties |

---
| __Key__ | com.openexchange.drive.directLinkQuota |
|:----------------|:--------|
| __Description__ | Configures the pattern for a direct link to manage a user's quota. <br>Text in brackets is replaced dynamically during link generation in the backend,<br>however, it's still possible to overwrite them here with a static value, or<br>even define an arbitrary URL here.<br>[protocol] is replaced automatically with the protocol used by the client<br>(typically "http" or "https").<br>[hostname] should be replaced with the server's canonical host name (if not,<br>the server tries to determine the hostname on it's own), <br>[uiwebpath] is replaced with the value of "com.openexchange.UIWebPath" as defined in<br>"server.properties", while [dispatcherPrefix] is replaced with the value of<br>"com.openexchange.dispatcher.prefix" ("server.properties", too).<br>[contextid], [userid] and [login] are replaced to reflect the values of the<br>current user.     <br> |
| __Default__ | [protocol]://[hostname] |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.directLinkHelp |
|:----------------|:--------|
| __Description__ | Configures the pattern for a direct link to the online help. This serves as<br>target for the "Help" section in the client applications. Text in brackets<br>is replaced dynamically during link generation in the backend, however, it's<br>still possible to overwrite them here with a static value, or even define an<br>arbitrary URL here.<br>[protocol] is replaced automatically with the protocol used by the client<br>(typically "http" or "https").<br>[hostname] should be replaced with the server's canonical host name (if not,<br>the server tries to determine the hostname on it's own), <br>[uiwebpath] is replaced with the value of "com.openexchange.UIWebPath" as defined in<br>"server.properties", while [dispatcherPrefix] is replaced with the value of<br>"com.openexchange.dispatcher.prefix" ("server.properties", too).<br>[contextid], [userid] and [login] are replaced to reflect the values of the<br>current user.   <br> |
| __Default__ | [protocol]://[hostname]/[uiwebpath]/help-drive/l10n/[locale]/index.html |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.mail.autoconfig.ispdb |
|:----------------|:--------|
| __Description__ | The ISPDB is a central database, currently hosted by Mozilla Messaging, but free to use for any client.<br>It contains settings for the world's largest ISPs.<br>We hope that the database will soon have enough information to autoconfigure approximately 50% of our user's email accounts.<br> |
| __Default__ | https://live.mozillamessaging.com/autoconfig/v1.1/ |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Autoconfig.html">Autoconfig</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | autoconfig.properties |

---
| __Key__ | com.openexchange.mail.autoconfig.http.proxy |
|:----------------|:--------|
| __Description__ | Provides the possibility to specify a proxy that is used to access any HTTP end-points. If empty, no proxy is used.<br>Notation is: <optional-protocol> + "://" + <proxy-host> + ":" + <proxy-port><br>             With "http" as fall-back protocol<br>E.g. "67.177.104.230:58720" (using HTTP protocol) or "https://78.0.25.45:8345" (using HTTPS protocol)<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Autoconfig.html">Autoconfig</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | autoconfig.properties |

---
| __Key__ | com.openexchange.hazelcast.network.join.static.nodes |
|:----------------|:--------|
| __Description__ | Configures a comma-separated list of IP addresses / hostnames of possible<br>nodes in the cluster, e.g. "10.20.30.12, 10.20.30.13:5701, 192.178.168.110".<br>Only used if "com.openexchange.hazelcast.network.join" is set to "static".<br>It doesn't hurt if the address of the local host appears in the list, so<br>that it's still possible to use the same list throughout all nodes in the<br>cluster.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.hazelcast.network.join |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.smtp[.primary].smtpLocalhost |
|:----------------|:--------|
| __Description__ | The localhost name that is going to be used on SMTP's HELO or EHLO command.<br>The default is set to InetAddress.getLocalHost().getHostName() but if either JDK or name service are not<br>configured properly, this routine fails and the HELO or EHLO command is send without a name which<br>leads to an error: "501 HELO requires domain address"<br>The value "null" falls back to InetAddress.getLocalHost().getHostName() which works in most cases.<br>Default is "null"<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | imap.properties |

---
| __Key__ | IMAP_SERVER |
|:----------------|:--------|
| __Description__ | IMAP server ip or fqdn.<br> |
| __Default__ | localhost |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP_Auth.html">IMAP Auth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | imapauth.properties |

---
| __Key__ | com.openexchange.caldav.url |
|:----------------|:--------|
| __Description__ | Tells users where to find a caldav folder. This can be displayed in frontends.<br>You can use the variables [hostname] and [folderId] <br>If you chose to deploy caldav as a virtual host (say 'dav.open-xchange.com') use<br>https://dav.open-xchange.com/caldav/[folderId] as the value<br>If you are using user-agent sniffing use<br>https://[hostname]/caldav/[folderId]        <br> |
| __Default__ | https://[hostname]/caldav/[folderId] |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CalDAV.html">CalDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | caldav.properties |

---
| __Key__ | com.openexchange.push.udp.remoteHost |
|:----------------|:--------|
| __Description__ | List of open-xchange servers that should be connected when multicast is disabled.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | push-udp.properties |

---
| __Key__ | com.openexchange.push.udp.hostname |
|:----------------|:--------|
| __Description__ | If empty, then the output of the java function getHostName will be used. <br>This name is used for internal communication.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
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
| __Key__ | com.openexchange.filestore.sproxyd.[filestoreID].path |
|:----------------|:--------|
| __Description__ | The path under which sproxyd is available. The path must lead to the namespace under<br>which OX related files shall be stored. It is expected that the namespace configuration<br>is available under <protocol>://<host>/<path>/.conf.<br>Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | filestore-sproxyd.properties |

---
| __Key__ | com.openexchange.twitter.http.proxyHost |
|:----------------|:--------|
| __Description__ | The HTTP proxy host.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.twitter.http.proxyPort |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Twitter.html">Twitter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | twitter.properties |

---
| __Key__ | mail.host |
|:----------------|:--------|
| __Description__ | The default host name of the mail server for both Stores and Transports. Used if the mail.protocol.host property isn't set.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.[protocol].host |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.[protocol].host |
|:----------------|:--------|
| __Description__ | The host name of the mail server for the specified protocol. Overrides the mail.host property.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.host |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.host |
|:----------------|:--------|
| __Description__ | The SMTP server to connect to.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.localhost |
|:----------------|:--------|
| __Description__ | Local host name. Defaults to InetAddress.getLocalHost().getHostName().<br>Should not normally need to be set if your JDK and your name service are configured properly.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.host |
|:----------------|:--------|
| __Description__ | The IMAP server to connect to.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.host |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.localaddress |
|:----------------|:--------|
| __Description__ | Local address (host name) to bind to when creating the IMAP socket. Defaults to the address picked by the Socket class.<br>Should not normally need to be set, but useful with multi-homed hosts where it's important to pick a particular local address to bind to.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.host |
|:----------------|:--------|
| __Description__ | The POP3 server to connect to.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.host |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.localaddress |
|:----------------|:--------|
| __Description__ | Local address (host name) to bind to when creating the POP3 socket. Defaults to the address picked by the Socket class.<br>Should not normally need to be set, but useful with multi-homed hosts where it's important to pick a particular local address to bind to.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | javamail.properties |

---
| __Key__ | URL |
|:----------------|:--------|
| __Description__ | URL to the config interface. %c is replaced with context login information.<br>%u is replaced with user login information. %p is replaced with user password.<br> |
| __Default__ | http://localhost/?cid=%c&login=%u&pass=%p |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Configjump.html">Configjump</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | configjump.properties |

---
| __Key__ | com.openexchange.saml.acsURL |
|:----------------|:--------|
| __Description__ | Sets the URL of the local assertion consumer service (ACS). This value is used within<br>authentication requests, compared against Destination attributes in IdP responses<br>and will be contained in the service providers metadata XML. The according endpoint<br>is always registered with '{prefix}/saml/acs' as servlet alias.<br><br>This property is mandatory.<br>Default: <empty><br>Example: https://appsuite.example.com/appsuite/api/saml/acs<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SAML.html">SAML</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | saml.properties |

---
| __Key__ | com.openexchange.saml.slsURL |
|:----------------|:--------|
| __Description__ | Sets the URL of the local single logout service. This value is compared against Destination<br>attributes in IdP responses and will be contained in the service providers metadata XML.<br>The according endpoint is always registered with '{prefix}/saml/sls' as servlet alias.<br><br>This property is mandatory if 'com.openexchange.saml.enableSingleLogout' is 'true'.<br>"Default: <empty>"<br>"Example: 'https://appsuite.example.com/appsuite/api/saml/sls'"<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SAML.html">SAML</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | saml.properties |

---
| __Key__ | com.openexchange.saml.idpAuthnURL |
|:----------------|:--------|
| __Description__ | The URL of the IdP endpoint where authentication requests are to be sent to.<br><br>This property is mandatory.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SAML.html">SAML</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | saml.properties |

---
| __Key__ | com.openexchange.saml.idpLogoutURL |
|:----------------|:--------|
| __Description__ | The URL of the IdP endpoint where logout requests are to be sent to.<br><br>This property is mandatory if 'com.openexchange.saml.enableSingleLogout' is 'true'.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SAML.html">SAML</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | saml.properties |

---
| __Key__ | com.openexchange.custom.spamexperts.imapurl |
|:----------------|:--------|
| __Description__ | the imap url to the spamexperts imap server<br><br>"Example1:" imap://myserver.example.com<br>if the imap server offers STARTTLS, communication will be encrypted<br><br>"Example2:" imaps:myserver.example.com:993<br>connect to port 993 using a secure connection directly<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spamexperts.html">Spamexperts</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | spamexperts.properties |

---
| __Key__ | JMXBindAddress |
|:----------------|:--------|
| __Description__ | Define the bind address for JMX agent.<br>Use value "\*" to let the JMX monitor bind to all interfaces.<br> |
| __Default__ | localhost |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Management.html">Management</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Monitoring.html">Monitoring</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | management.properties |

---
