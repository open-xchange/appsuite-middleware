---
title: White List
---

This page shows all properties with the tag: White List

| __Key__ | com.openexchange.IPCheckWhitelist |
|:----------------|:--------|
| __Description__ | Specify a comma-separated list of client patterns that do bypass IP check<br>E.g. com.openexchange.IPCheckWhitelist="Mobile App\*", "Foo\*"<br> |
| __Default__ | open-xchange-mailapp, open-xchange-mobile-api-facade |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/White_List.html">White List</a> |
| __File__ | Server.properties |

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
| __Key__ | com.openexchange.net.ssl.whitelist |
|:----------------|:--------|
| __Description__ | Defines a comma separated list of hosts certificates shouldn't be checked for validity. The list can contain wildcards and ip ranges. <br>In addition it is possible to define a list by host name, IPv4 or IPv6 address. An incoming host name will not be checked against its IP address, <br>for instance connecting against 'imap.gmail.com' will be possible if '\*.gmail.com' is whitelisted but adding only the corresponding IP address entry '64.233.167.108' as whitelisted won't work.   <br> |
| __Default__ | 127.0.0.1-127.255.255.255,localhost |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SSL.html">SSL</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/White_List.html">White List</a> |
| __File__ | ssl.properties |

---
| __Key__ | com.openexchange.messaging.rss.feed.whitelist.ports |
|:----------------|:--------|
| __Description__ | Specifies a white-list for such ports that are allowed to connect against when adding/updating a RSS feed.<br>No port will always be accepted. <br>An empty value means no white-listing is active.<br> |
| __Default__ | 80,443 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/RSS.html">RSS</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/White_List.html">White List</a> |
| __File__ | rssmessaging.properties |

---
| __Key__ | com.openexchange.publish.microformats.usesWhitelisting |
|:----------------|:--------|
| __Description__ | If set to true, the contents of a microformatted publication will<br>be processed through a whitelisting filter that removes elements<br>that are considered potentially unsafe. We recommend you use this<br>in case you do not have publications on a different subdomain.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Publication.html">Publication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/White_List.html">White List</a> |
| __File__ | publications.properties |

---
| __Key__ | com.openexchange.subscribe.microformats.allowedHosts |
|:----------------|:--------|
| __Description__ | Optionally specifies the list of accepted host names allowed being subscribed.<br>If property is empty, there is no restriction for such subscriptions.<br>Otherwise non-matching host names are rejected. <br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Microformats.html">Microformats</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Subscribe.html">Subscribe</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/White_List.html">White List</a> |
| __File__ | microformatSubscription.properties |

---
