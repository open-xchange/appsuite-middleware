---
title: SMTP
---

To differentiate between settings that apply all SMTP accounts or settings that only apply to the primary account
each property containing the "[.primary]" suffix allows to specify a value only applicable to the primary account
by appending the "primary." suffix to properties' common "com.openexchange.smtp." prefix..
E.g.
"com.openexchange.smtp.smtpTimeout=50000" specifies 50sec read timeout for every SMTP account
"com.openexchange.smtp.primary.smtpTimeout=20000" specifies 20sec read timeout for primary-only SMTP account


| __Key__ | com.openexchange.smtp[.primary].smtpLocalhost |
|:----------------|:--------|
| __Description__ | The localhost name that is going to be used on SMTP's HELO or EHLO command.<br>The default is set to InetAddress.getLocalHost().getHostName() but if either JDK or name service are not<br>configured properly, this routine fails and the HELO or EHLO command is send without a name which<br>leads to an error: "501 HELO requires domain address"<br>The value "null" falls back to InetAddress.getLocalHost().getHostName() which works in most cases.<br>Default is "null"<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.smtp[.primary].smtpAuthEnc |
|:----------------|:--------|
| __Description__ | Define the encoding for SMTP authentication<br>Default is UTF-8<br> |
| __Default__ | UTF-8 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.smtp.setSMTPEnvelopeFrom |
|:----------------|:--------|
| __Description__ | Defines if SMTP header ENVELOPE-FROM should be explicitly set to<br>user's primary email address or not<br>Default is false<br>Applies only to primary SMTP account; considered as "false" for every external SMTP account.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.smtp[.primary].smtpTimeout |
|:----------------|:--------|
| __Description__ | Define the socket read timeout value in milliseconds. A value less than<br>or equal to zero is infinite timeout. See also mail.smtp.timeout<br>Default is 50000<br> |
| __Default__ | 50000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.smtp[.primary].smtpConnectionTimeout |
|:----------------|:--------|
| __Description__ | Define the socket connect timeout value in milliseconds. A value less<br>or equal to zero is infinite timeout. See also mail.smtp.connectiontimeout<br>Default is 10000<br> |
| __Default__ | 10000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.smtp.logTransport |
|:----------------|:--------|
| __Description__ | Specifies whether a transported message shall be logged providing "Message-Id" header, login, and SMTP server information<br>Default is false<br>Applies only to primary SMTP account; considered as "false" for every external SMTP account<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.smtp[.primary].ssl.protocols |
|:----------------|:--------|
| __Description__ | Specifies the SSL protocols that will be enabled for SSL connections. The property value is a whitespace separated list of tokens.<br>Default is empty<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.smtp[.primary].ssl.ciphersuites |
|:----------------|:--------|
| __Description__ | Specifies the SSL cipher suites that will be enabled for SSL connections. The property value is a whitespace separated list of tokens.<br><br>Check "http://<ox-grizzly-hostname>:<ox-grizzly-port>/stats/diagnostic?param=ciphersuites" to check available cipher suites.<br><br>Default value is empty (fall-back to current JVM's default SSL cipher suite)<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.smtp.sendPartial |
|:----------------|:--------|
| __Description__ | Whether partial send is allowed or message transport is supposed to be aborted<br>Default is "false"<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | imap.properties |

---
