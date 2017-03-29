---
title: Mail Filter
---

| __Key__ | com.openexchange.mail.filter.loginType |
|:----------------|:--------|
| __Description__ | Specifies which sieve server should be used. Two options are allowed here:<br>user : use the imap server setting stored for user in the database<br>global : use the sieve server given in com.openexchange.mail.filter.server for all users.<br> |
| __Default__ | user |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mail.filter.server |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.credentialSource |
|:----------------|:--------|
| __Description__ | Specifies which sieve credentials should be use. Four options are allowed here:<br>"session" : login name and password are used from the current session.<br>"session-full-login" : full login (incl. context part) name and password are used from the current session.<br>"imapLogin" : the login name is taken from the field imapLogin of the current<br>              user the password is taken from the current session.<br>"mail" : use the primary mail address of the user and the password from the session.<br> |
| __Default__ | session |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

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
| __Key__ | com.openexchange.mail.filter.port |
|:----------------|:--------|
| __Description__ | Specifies the SIEVE port<br>-----------------------------------------------------------<br>NOTE: 2000 is the deprecated port number for SIEVE (now assigned to some Cisco SCCP protocol by the IANA)<br>      4190 is the new one used with most recent Linux and IMAP implementations.<br>Please check your system's default port defined at /etc/services.<br>-----------------------------------------------------------<br> |
| __Default__ | 4190 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.scriptName |
|:----------------|:--------|
| __Description__ | Specifies the name of the script, which will be generated.<br>Note that the mail filter bundle will leave the old script with the old<br>script name behind, and doesn't delete it.<br> |
| __Default__ | Open-Xchange |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.authenticationEncoding |
|:----------------|:--------|
| __Description__ | Define the charset encoding to use for authentication to sieve server.<br> |
| __Default__ | UTF-8 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.nonRFCCompliantTLSRegex |
|:----------------|:--------|
| __Description__ | Define the regex which recognizes servers with incorrect sieve TLS implementation.<br> |
| __Default__ | ^Cyrus.\*v([0-1]\.[0-9].\*&#124;2\.[0-2].\*&#124;2\.3\.[0-9]&#124;2\.3\.[0-9][^0-9].\*)$ |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.tls |
|:----------------|:--------|
| __Description__ | Whether to use TLS if available.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Security.html">Security</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.vacationDomains |
|:----------------|:--------|
| __Description__ | Specifies if a vacation messages should only be sent to specific domains.<br>If multiple domains are given, they should be separated by ","<br>e.g. com.openexchange.mail.filter.vacationDomains=testdomain.com,example.com<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

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
| __Key__ | com.openexchange.mail.filter.passwordSource |
|:----------------|:--------|
| __Description__ | Set the password source; meaning which source is taken to determine a user's password to login into mail filter system. <br>If 'session' is set, then user's individual system's password is taken. <br>If 'global' is set, then the value specified through property 'com.openexchange.mail.filter.masterPassword' is taken.<br>Currently known values: session and global<br> |
| __Default__ | session |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mail.filter.masterPassword |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.masterPassword |
|:----------------|:--------|
| __Description__ | The master password for mail/transport server. Only takes effect when property<br>"com.openexchange.mail.filter.passwordSource" is set to "global".<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mail.filter.passwordSource |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.useUTF7FolderEncoding |
|:----------------|:--------|
| __Description__ | This property defines if mailbox names shall be UTF-7 encoded as specified in RFC2060; section 5.1.3. "Mailbox International Naming Convention".<br>If set to "false" no UTF-7 encoding is performed.<br><br>Set to "true" for those Cyrus IMAP server versions that do NOT support "sieve_utf8fileinto" property (e.g. lower than v2.3.11)<br>Set to "true" for those Cyrus IMAP server versions that support "sieve_utf8fileinto" property having that property set to "0".<br>Thus moving mails with the 'fileinto' command will properly work for mailbox names that contain non-ascii characters.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.punycode |
|:----------------|:--------|
| __Description__ | Enable punycode encoding for the username used in authentication against the managesieve server.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.useSIEVEResponseCodes |
|:----------------|:--------|
| __Description__ | Interpret SIEVE Response Codes, see https://tools.ietf.org/html/rfc5804#section-1.3<br>In most cases, this option must be kept to false.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.redirectWhitelist |
|:----------------|:--------|
| __Description__ | Specifies a comma-separated list of domains (wild-card syntax supported) that are allowed for redirect rules.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/White_List.html">White List</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.preferredSaslMech |
|:----------------|:--------|
| __Description__ | Specifies the preferred SASL authentication mechanism.<br>An empty value falls-back to "PLAIN"<br><br>Known values: GSSAPI, XOAUTH2, OAUTHBEARER<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

---
