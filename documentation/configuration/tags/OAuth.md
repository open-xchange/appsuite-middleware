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
| __Key__ | com.openexchange.oauth.[serviceId] |
|:----------------|:--------|
| __Description__ | Enables or disables the oauth service with the service id [serviceId]. <br><br>Currently known service ids:<br>  \* boxcom<br>  \* dropbox<br>  \* google<br>  \* linkedin<br>  \* msliveconnect<br>  \* twitter<br>  \* xing<br>  \* yahoo<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/OAuth.html">OAuth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Boxcom.html">Boxcom</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Dropbox.html">Dropbox</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Google.html">Google</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LinkedIn.html">LinkedIn</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/MSLiveConnect.html">MSLiveConnect</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Twitter.html">Twitter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Xing.html">Xing</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Yahoo.html">Yahoo</a> |

---
| __Key__ | com.openexchange.oauth.[serviceId].apiKey |
|:----------------|:--------|
| __Description__ | The api key of your [serviceId] application.<br><br>See com.openexchange.oauth.[serviceId] for a list of currently known service ids.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/OAuth.html">OAuth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Boxcom.html">Boxcom</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Dropbox.html">Dropbox</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Google.html">Google</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LinkedIn.html">LinkedIn</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/MSLiveConnect.html">MSLiveConnect</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Twitter.html">Twitter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Xing.html">Xing</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Yahoo.html">Yahoo</a> |

---
| __Key__ | com.openexchange.oauth.[serviceId].apiSecret |
|:----------------|:--------|
| __Description__ | The api secret of your [serviceId] application.<br><br>See com.openexchange.oauth.[serviceId] for a list of currently known service ids.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/OAuth.html">OAuth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Boxcom.html">Boxcom</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Dropbox.html">Dropbox</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Google.html">Google</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LinkedIn.html">LinkedIn</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/MSLiveConnect.html">MSLiveConnect</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Twitter.html">Twitter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Xing.html">Xing</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Yahoo.html">Yahoo</a> |

---
| __Key__ | com.openexchange.oauth.[serviceId].redirectUrl |
|:----------------|:--------|
| __Description__ | The redirect url of your [serviceId] application.<br>E.g. "https://myappsuite.mydomain.invalid/ajax/defer"<br><br>See com.openexchange.oauth.[serviceId] for a list of currently known service ids.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/OAuth.html">OAuth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Boxcom.html">Boxcom</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Dropbox.html">Dropbox</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Google.html">Google</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/MSLiveConnect.html">MSLiveConnect</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Yahoo.html">Yahoo</a> |

---
| __Key__ | com.openexchange.oauth.[serviceId].productName |
|:----------------|:--------|
| __Description__ | The product name of your [serviceId] application.<br><br>See com.openexchange.oauth.[serviceId] for a list of currently known service ids.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/OAuth.html">OAuth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Dropbox.html">Dropbox</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Google.html">Google</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Yahoo.html">Yahoo</a> |

---
| __Key__ | com.openexchange.oauth.xing.consumerKey |
|:----------------|:--------|
| __Description__ | The consumer key (for upsell).<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/OAuth.html">OAuth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Xing.html">Xing</a> |

---
| __Key__ | com.openexchange.oauth.xing.consumerSecret |
|:----------------|:--------|
| __Description__ | The consumer secret (for upsell)<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/OAuth.html">OAuth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Xing.html">Xing</a> |

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
