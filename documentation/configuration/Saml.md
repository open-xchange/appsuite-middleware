---
title: Saml
---

| __Key__ | com.openexchange.saml.enableSessionIndexAutoLogin |
|:----------------|:--------|
| __Description__ | Specifies whether SAML-specific auto-login is enabled, that uses the SessionIndex of the AuthnResponse.<br> |
| __Default__ | false |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | saml.properties |

---
| __Key__ | com.openexchange.saml.oauth.tokenEndpoint |
|:----------------|:--------|
| __Description__ | Specifies the OAuth HTTP token end-point.<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __File__ | saml.properties |

---
| __Key__ | com.openexchange.saml.oauth.clientId |
|:----------------|:--------|
| __Description__ | Specifies the client id used to authenticate the client against the OAuth token end-point.<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.saml.oauth.tokenEndpoint |
| __File__ | saml.properties |

---
| __Key__ | com.openexchange.saml.oauth.clientSecret |
|:----------------|:--------|
| __Description__ | Specifies the client secret used to authenticate the client against the OAuth token end-point.<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.saml.oauth.tokenEndpoint |
| __File__ | saml.properties |

---
