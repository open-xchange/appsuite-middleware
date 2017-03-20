---
title: White List
---

This page shows all properties with the tag: White List

| __Key__ | com.openexchange.net.ssl.whitelist |
|:----------------|:--------|
| __Description__ | Defines a comma separated list of hosts certificates shouldn't be checked for validity. The list can contain wildcards and ip ranges. In addition it is possible to define a list by host name, IPv4 or IPv6 address. An incoming host name will not be checked against its IP address, for instance connecting against 'imap.gmail.com' will be possible if '\*.gmail.com' is whitelisted but adding only the corresponding IP address entry '64.233.167.108' as whitelisted won't work.   <br> |
| __Default__ | 127.0.0.1-127.255.255.255,localhost |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SSL.html">SSL</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/White_List.html">White List</a> |
| __File__ | ssl.properties |

---
