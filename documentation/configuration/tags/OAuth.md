---
title: OAuth
---

This page shows all properties with the tag: OAuth

| __Key__ | com.openexchange.mail.authType |
|:----------------|:--------|
| __Description__ | Specifies the authentication type which should be used for primary account's mail access. Known values: 'login', 'xoauth2', and "oauthbearer"<br> |
| __Default__ | login |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/OAuth.html">OAuth</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.transport.authType |
|:----------------|:--------|
| __Description__ | Specifies the authentication type which should be used for primary account's mail transport. Known values: 'login', 'xoauth2', and "oauthbearer"<br> |
| __Default__ | login |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/OAuth.html">OAuth</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.oauth.modules.enabled.[oauth_provider] |
|:----------------|:--------|
| __Description__ | A comma seperated list of enabled oauth modules. <br>This list can be configured for each individual oauth provider. <br>To identify the oauth provider replace [oauth_provider] with the last part of the provider id.<br>E.g. com.openexchange.oauth.google -> com.openexchange.oauth.modules.enabled.google<br>Available modules are:<br> -mail<br> -calendar_ro<br> -contacts_ro<br> -calendar<br> -contacts<br> -drive<br> -generic <br> |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/OAuth.html">OAuth</a> |
| __File__ | oauth.properties |

---
| __Key__ | com.openexchange.saml.oauth.tokenEndpoint |
|:----------------|:--------|
| __Description__ | Specifies the OAuth HTTP token end-point.<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SAML.html">SAML</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/OAuth.html">OAuth</a> |
| __File__ | saml.properties |

---
| __Key__ | com.openexchange.saml.oauth.clientId |
|:----------------|:--------|
| __Description__ | Specifies the client id used to authenticate the client against the OAuth token end-point.<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.saml.oauth.tokenEndpoint |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SAML.html">SAML</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/OAuth.html">OAuth</a> |
| __File__ | saml.properties |

---
| __Key__ | com.openexchange.saml.oauth.clientSecret |
|:----------------|:--------|
| __Description__ | Specifies the client secret used to authenticate the client against the OAuth token end-point.<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.saml.oauth.tokenEndpoint |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SAML.html">SAML</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/OAuth.html">OAuth</a> |
| __File__ | saml.properties |

---
