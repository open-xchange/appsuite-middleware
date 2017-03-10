# OAuth

| __Key__ | com.openexchange.oauth.modules.enabled.[oauth_provider] |
|:----------------|:--------|
| __Description__ | A comma seperated list of enabled oauth modules. <br>This list can be configured for each individual oauth provider. <br>To identify the oauth provider replace [oauth_provider] with the last part of the provider id.<br>E.g. com.openexchange.oauth.google -> com.openexchange.oauth.modules.enabled.google<br>Available modules are:<br> -mail<br> -calendar_ro<br> -contacts_ro<br> -calendar<br> -contacts<br> -drive<br> -generic <br> |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __File__ | oauth.properties |

---
