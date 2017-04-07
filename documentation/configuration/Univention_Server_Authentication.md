---
title: Univention Server Authentication
---

| __Key__ | com.openexchange.authentication.ucs.useLdapPool |
|:----------------|:--------|
| __Description__ | Specifies whether to se ldap pooling or not.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Univention.html">Univention</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a> |
| __File__ | authplugin.properties |

---
| __Key__ | com.openexchange.authentication.ucs.baseDn |
|:----------------|:--------|
| __Description__ | The basedn of ldap directory.<br> |
| __Default__ | dc=example,dc=org |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Univention.html">Univention</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a> |
| __File__ | authplugin.properties |

---
| __Key__ | com.openexchange.authentication.ucs.ldapUrl |
|:----------------|:--------|
| __Description__ | The ldap url; use ldaps:// for ssl.<br> |
| __Default__ | ldap://localhost |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Univention.html">Univention</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | authplugin.properties |

---
| __Key__ | com.openexchange.authentication.ucs.mailAttribute |
|:----------------|:--------|
| __Description__ | Specifies the attribute containing the email address from which domain part will be used to identify the context.<br> |
| __Default__ | mailPrimaryAddress |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Univention.html">Univention</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | authplugin.properties |

---
| __Key__ | com.openexchange.authentication.ucs.loginAttribute |
|:----------------|:--------|
| __Description__ | Specifies the ldap attribute containing the OX Login name.<br> |
| __Default__ | uid |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Univention.html">Univention</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a> |
| __File__ | authplugin.properties |

---
| __Key__ | com.openexchange.authentication.ucs.contextIdAttribute |
|:----------------|:--------|
| __Description__ | Specifies the name of the attribute containing the contextId in order to lookup the context.<br>This is optional; if not specified, context lookup will be done using domain name as found <br>in com.openexchange.authentication.ucs.mailAttribute.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.authentication.ucs.mailAttribute |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Univention.html">Univention</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | authplugin.properties |

---
| __Key__ | com.openexchange.authentication.ucs.searchFilter |
|:----------------|:--------|
| __Description__ | Search query to find the user within ldap.<br>%s will be replaced by the login as entered in the ox login mask.<br> |
| __Default__ | (&(objectClass=oxUserObject)(&#124;(uid=%s)(mailPrimaryAddress=%s))) |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Univention.html">Univention</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a> |
| __File__ | authplugin.properties |

---
| __Key__ | com.openexchange.authentication.ucs.passwordChangeURL |
|:----------------|:--------|
| __Description__ | Where to redirect users that need to change their password when it is expired.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Univention.html">Univention</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | authplugin.properties |

---
| __Key__ | com.openexchange.authentication.ucs.bindDn |
|:----------------|:--------|
| __Description__ | Optionally specify dn to be used to bind to ldap server instead of doing anonymous access.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.authentication.ucs.bindPassword |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Univention.html">Univention</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a> |
| __File__ | authplugin.properties |

---
| __Key__ | com.openexchange.authentication.ucs.bindPassword |
|:----------------|:--------|
| __Description__ | Password for specified binddn.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.authentication.ucs.bindDn |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Univention.html">Univention</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | authplugin.properties |

---
