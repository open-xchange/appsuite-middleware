---
title: LDAP
---

This page shows all properties with the tag: LDAP

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
| __Key__ | com.openexchange.authentication.ucs.loginAttribute |
|:----------------|:--------|
| __Description__ | Specifies the ldap attribute containing the OX Login name.<br> |
| __Default__ | uid |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Univention.html">Univention</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a> |
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
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Univention.html">Univention</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a> |
| __File__ | authplugin.properties |

---
| __Key__ | java.naming.provider.url |
|:----------------|:--------|
| __Description__ | URL of the LDAP server to connect to for authenticating users.<br>ldaps is supported.<br> |
| __Default__ | ldap://localhost:389/dc=example,dc=com |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | ldapauth.properties |

---
| __Key__ | java.naming.security.authentication |
|:----------------|:--------|
| __Description__ | Defines the authentication security that should be used.<br> |
| __Default__ | simple |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | ldapauth.properties |

---
| __Key__ | com.sun.jndi.ldap.connect.timeout |
|:----------------|:--------|
| __Description__ | Timeouts are useful to get quick responses for login requests. <br>This timeout is used if a new connection is established.<br> |
| __Default__ | 10000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | ldapauth.properties |

---
| __Key__ | com.sun.jndi.ldap.read.timeout |
|:----------------|:--------|
| __Description__ | This timeout only works since Java 6 SE to time out waiting for a response.<br> |
| __Default__ | 10000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | ldapauth.properties |

---
| __Key__ | useFullLoginInfo |
|:----------------|:--------|
| __Description__ | Set to true to authenticate with the string entered in the login name field, e.g.<br>"user@domain" instead of just "user" against ldap server.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | ldapauth.properties |

---
| __Key__ | uidAttribute |
|:----------------|:--------|
| __Description__ | This attribute is used for login. E.g. uid=<login>,baseDN<br>NOTE: <br>If the attribute is not part of the user dn, you need to set bindOnly=false.<br> |
| __Default__ | uid |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | ldapauth.properties |

---
| __Key__ | baseDN |
|:----------------|:--------|
| __Description__ | This is the base distinguished name where the user are located.<br> |
| __Default__ | ou=Users,ou=OxObjects,dc=open-xchange,dc=com |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | ldapauth.properties |

---
| __Key__ | ldapReturnField |
|:----------------|:--------|
| __Description__ | If you do not want to pass on the user id, used for authentication, to the<br>groupware but another field entry of the LDAP user object, then you can<br>specify the field here.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | ldapauth.properties |

---
| __Key__ | adsBind |
|:----------------|:--------|
| __Description__ | This option activates a special ADS bind. It allows the user to<br>authenticate at the ADS with only the displayName or by using the syntax<br>DOMAIN\Samaccountname.<br>Note that "\" cannot be used in user names when this option is activated.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | ldapauth.properties |

---
| __Key__ | bindOnly |
|:----------------|:--------|
| __Description__ | Set bindonly to false if the user entries are not directly contained in the<br>entry of the dn, and you have to search within ldap, change ldapScope<br>in order to define the search depth.<br>NOTE: When two users exist with the same uidAttribute, authentication<br>will be refused, though.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | ldapauth.properties |

---
| __Key__ | ldapScope |
|:----------------|:--------|
| __Description__ | Set the ldap search scope in case bindOnly is set to false<br>must be one of subtree, onelevel or base.<br> |
| __Default__ | subtree |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | ldapauth.properties |

---
| __Key__ | searchFilter |
|:----------------|:--------|
| __Description__ | Specify the filter to limit the search of user entries (used in combination with subtreeSearch=true).<br>The filter will be ANDed with the attributed specified in uidAttribute.<br>Example:<br>(&(objectclass=posixAccount)(uid=foo))<br> |
| __Default__ | (objectclass=posixAccount) |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | ldapauth.properties |

---
| __Key__ | bindDN |
|:----------------|:--------|
| __Description__ | If your LDAP server does not allow to do searches without any authentication,<br>specify a dn here to bind in order to search (used in combination with subtreeSearch=true).<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | ldapauth.properties |

---
| __Key__ | bindDNPassword |
|:----------------|:--------|
| __Description__ | The password required for the bindDN.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | ldapauth.properties |

---
| __Key__ | referral |
|:----------------|:--------|
| __Description__ | This option configures how to handle the chasing of referrals in LDAP <br>(see http://java.sun.com/products/jndi/tutorial/ldap/referral/overview.html).<br>possible values: ignore, follow, throw<br> |
| __Default__ | follow |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | ldapauth.properties |

---
| __Key__ | proxyUser |
|:----------------|:--------|
| __Description__ | Comma separated list of login names allowed to login as a proxy for every other user.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | ldapauth.properties |

---
| __Key__ | proxyDelimiter |
|:----------------|:--------|
| __Description__ | Define a delimiter to be used to seperate Proxy from Userlogin<br>If defined, a proxy user can login on behalf of a user using the form<br><PROXYACCOUNT><DELIMITER><USERACCOUNT><br>NOTES:<br>    1. The underlying Mailserver must support SASL AUTHPROXYING.<br>       The open-xchange mailfilterbundle does not support it, so it will<br>       raise errors.<br>    2. The same option has to be set in mail.properties<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | ldapauth.properties |

---
