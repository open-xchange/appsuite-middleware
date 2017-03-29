---
title: Secret
---

| __Key__ | com.openexchange.secret.secretSource |
|:----------------|:--------|
| __Description__ | Specifies the source for secret (used to decrypt/encrypt user passwords) <password>    Denotes the session's password (also the fall-back to previous handling) <user-id>     Denotes the user identifier <context-id>  Denotes the context identifier <random>      Denotes the value specified in property 'com.openexchange.secret.secretRandom' <list>        Step-wise trial-and-error with tokens specified in file 'secrets' Literals are surrounded by single-quotes E.g. com.openexchange.secret.secretSource="<user-id> + '@' + <context-id>" |
| __Default__ | <password> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Secret.html">Secret</a> |
| __File__ | secret.properties |

---
| __Key__ | com.openexchange.secret.secretRandom |
|:----------------|:--------|
| __Description__ | The random secret token |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Secret.html">Secret</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | secret.properties |

---
| __Key__ | com.openexchange.secret.recovery.fast.enabled |
|:----------------|:--------|
| __Description__ | Specifies if the special fast crypt token check is enabled or not.<br>That mechanism is used to check the validity of user's current secret in a fast manner and is used when triggering the "recovery/secret?action=check" call. |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Secret.html">Secret</a> |
| __File__ | secret.properties |

---
