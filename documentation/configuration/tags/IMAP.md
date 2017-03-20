---
title: IMAP
---

This page shows all properties with the tag: IMAP

| __Key__ | com.openexchange.imap.greeting.host.regex |
|:----------------|:--------|
| __Description__ | Specifies the regular expression to use to extract the host name/IP address information out of the greeting string advertised by primary<br>IMAP server. Only applicable for primary IMAP server! Default is empty.<br><br>The regular expression is supposed to be specified in Java notation: http://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html<br><br>Moreover, either the complete regex is considered or in case a capturing group is present that group will be preferred.<br>I.e. "Dovecot at ([0-9a-zA-Z._-]\*) is ready", then the capturing group is supposed to extract the host name/IP addres information<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
