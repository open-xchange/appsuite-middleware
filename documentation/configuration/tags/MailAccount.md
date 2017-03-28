---
title: MailAccount
---

This page shows all properties with the tag: MailAccount

| __Key__ | com.openexchange.mail.account.blacklist |
|:----------------|:--------|
| __Description__ | Specifies a black-list for those hosts that are covered by denoted IP range; e.g. "127.0.0.1-127.255.255.255, localhost, internal.domain.org"<br>Creation of mail accounts with this hosts will be prevented. Also the validation of those accounts will fail.<br>An empty value means no black-listing is active<br>Default is "127.0.0.1-127.255.255.255,localhost"<br> |
| __Default__ | 127.0.0.1-127.255.255.255,localhost |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/MailAccount.html">MailAccount</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.account.whitelist.ports |
|:----------------|:--------|
| __Description__ | Specifies a white-list for such ports that are allowed to connect against when setting up/validating an external mail account<br>An empty value means no white-listing is active<br>Default is: 143,993, 25,465,587, 110,995<br> |
| __Default__ | 143,993, 25,465,587, 110,995 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/MailAccount.html">MailAccount</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/White_List.html">White List</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.imageHost |
|:----------------|:--------|
| __Description__ | Specifies the host/domain from which to load inlined images contained in message content<br>Example "com.openexchange.mail.imageHost=http://my.imagehost.org".<br>In case no protocol/schema is specified, "http" is assumed by default<br>Default is empty; meaning to load from originating host<br>Exemplary setup:<br>- Artificially add a host name to /etc/hosts:<br>  127.0.0.1     imageserver.open-xchange.com<br>  - Enable the "com.openexchange.mail.imageHost" property in mail.properties:<br>    com.openexchange.mail.imageHost=http://imageserver.open-xchange.com<br>    - Check a mail with an inline image<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/MailAccount.html">MailAccount</a> |
| __File__ | mail.properties |

---
