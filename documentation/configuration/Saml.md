---
title: Saml
---

| __Key__ | com.openexchange.saml.enabled |
|:----------------|:--------|
| __Description__ | Must be set to 'true' to enable the feature, otherwise it is fully deactivated.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | saml.properties |

---
| __Key__ | com.openexchange.saml.enableMetadataService |
|:----------------|:--------|
| __Description__ | Whether the SPs metadata XML shall be made available via HTTP. The according<br>servlet will then be available under 'http(s)://{hostname}/{prefix}/saml/metadata'.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | saml.properties |

---
| __Key__ | com.openexchange.saml.enableSingleLogout |
|:----------------|:--------|
| __Description__ | Whether the single logout profile is enabled.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | saml.properties |

---
| __Key__ | com.openexchange.saml.entityID |
|:----------------|:--------|
| __Description__ | Sets the entity ID of the service provider. This property is mandatory.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | saml.properties |

---
| __Key__ | com.openexchange.saml.providerName |
|:----------------|:--------|
| __Description__ | Sets the human-readable name of the service provider. This property is mandatory.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | saml.properties |

---
| __Key__ | com.openexchange.saml.acsURL |
|:----------------|:--------|
| __Description__ | Sets the URL of the local assertion consumer service (ACS). This value is used within<br>authentication requests, compared against Destination attributes in IdP responses<br>and will be contained in the service providers metadata XML. The according endpoint<br>is always registered with '{prefix}/saml/acs' as servlet alias.<br><br>This property is mandatory.<br>Default: <empty><br>Example: https://appsuite.example.com/appsuite/api/saml/acs<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | saml.properties |

---
| __Key__ | com.openexchange.saml.slsURL |
|:----------------|:--------|
| __Description__ | Sets the URL of the local single logout service. This value is compared against Destination<br>attributes in IdP responses and will be contained in the service providers metadata XML.<br>The according endpoint is always registered with '{prefix}/saml/sls' as servlet alias.<br><br>This property is mandatory if 'com.openexchange.saml.enableSingleLogout' is 'true'.<br>"Default: <empty>"<br>"Example: 'https://appsuite.example.com/appsuite/api/saml/sls'"<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | saml.properties |

---
| __Key__ | com.openexchange.saml.logoutResponseBinding |
|:----------------|:--------|
| __Description__ | The binding via which logout responses shall be sent to the IdP on IdP-initiated single<br>logout flows. Must be 'http-redirect' or 'http-post'<br><br>This property is mandatory if 'com.openexchange.saml.enableSingleLogout' is 'true'.<br> |
| __Default__ | http-redirect |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | saml.properties |

---
| __Key__ | com.openexchange.saml.logoutResponseTemplate |
|:----------------|:--------|
| __Description__ | The HTML template to use when logout responses are sent to the IdP via HTTP POST.<br>The template must be located in '/opt/open-xchange/templates'.<br><br>This property is mandatory if 'com.openexchange.saml.enableSingleLogout' is 'true'<br>and 'com.openexchange.saml.logoutResponseBinding' is set to 'http-post'.<br> |
| __Default__ | saml.logout.response.html.tmpl |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | saml.properties |

---
| __Key__ | com.openexchange.saml.idpEntityID |
|:----------------|:--------|
| __Description__ | The entity ID of the IdP. It will be used to validate the 'Issuer' elements of SAML responses.<br><br>This property is mandatory.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | saml.properties |

---
| __Key__ | com.openexchange.saml.idpAuthnURL |
|:----------------|:--------|
| __Description__ | The URL of the IdP endpoint where authentication requests are to be sent to.<br><br>This property is mandatory.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | saml.properties |

---
| __Key__ | com.openexchange.saml.idpLogoutURL |
|:----------------|:--------|
| __Description__ | The URL of the IdP endpoint where logout requests are to be sent to.<br><br>This property is mandatory if 'com.openexchange.saml.enableSingleLogout' is 'true'.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | saml.properties |

---
| __Key__ | com.openexchange.saml.enableAutoLogin |
|:----------------|:--------|
| __Description__ | It is possible to enable a special kind of auto login mechanism that allows user agents to<br>re-use an existing OX session if it was created during the same browser session. If enabled,<br>a special cookie will be set, which is linked to the OX session and bound to the browser sessions<br>life time. The advantage of this mechanism is, that sessions are simply re-entered if the user<br>refreshes his browser window. He is then also able to open more than one tab of OX App Suit<br>at the same time. This mechanism can only re-use sticky sessions, i.e. it is mandatory that the<br>requests are always routed to the same backend for a certain session.<br><br>--- SECURITY WARNING ---<br>Enabling this setting is not compliant to the SAML specification as it bypasses the IdP in<br>certain cases. Additionally in scenarios where a public device is used, a foreign user might<br>take over a formerly authenticated users session if that user forgets to log out and doesn't<br>close his web browser (even if he closes the App Suite tab). As no login screen is displayed<br>by OX in SAML environments, the user is even not able to decide, whether the application shall<br>remember him or not.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | saml.properties |

---
| __Key__ | com.openexchange.saml.allowUnsolicitedResponses |
|:----------------|:--------|
| __Description__ | Whether unsolicited responses will be accepted or not.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | saml.properties |

---
| __Key__ | com.openexchange.saml.enableSessionIndexAutoLogin |
|:----------------|:--------|
| __Description__ | Specifies whether SAML-specific auto-login is enabled, that uses the SessionIndex of the AuthnResponse.<br> |
| __Default__ | false |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SAML.html">SAML</a> |
| __File__ | saml.properties |

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
