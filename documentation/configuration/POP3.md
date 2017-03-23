---
title: POP3
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
