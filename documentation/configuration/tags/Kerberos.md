---
title: Kerberos
---

This page shows all properties with the tag: Kerberos

| __Key__ | com.openexchange.kerberos.moduleName |
|:----------------|:--------|
| __Description__ | Name of the module in the authentication and authorization configuration file. Must be an entry in the file named by<br>java.security.auth.login.config.<br> |
| __Default__ | Open-Xchange |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Kerberos.html">Kerberos</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | kerberos.properties |

---
| __Key__ | com.openexchange.kerberos.userModuleName |
|:----------------|:--------|
| __Description__ | Name of the module in the authentication and authorization configuration file used for username and password authentication. Must be an<br>entry in the file named by java.security.auth.login.config.<br> |
| __Default__ | Open-Xchange-User-Auth |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Kerberos.html">Kerberos</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | kerberos.properties |

---
| __Key__ | com.openexchange.kerberos.proxyDelimiter |
|:----------------|:--------|
| __Description__ | Define a delimiter to be used to separate proxy authentication from normal user login on the frontend login screen. If defined, a proxy<br>user can login on behalf of a user using the form <PROXYACCOUNT><DELIMITER><USERACCOUNT>.<br>NOTE: Login to the mail server is not supported. The implementation for mail filter implementation based on SIEVE protocol does not<br>support a proxy authentication, so it will raise error messages.<br> |
| __Default__ | + |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Kerberos.html">Kerberos</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | kerberos.properties |

---
| __Key__ | com.openexchange.kerberos.proxyUser |
|:----------------|:--------|
| __Description__ | Comma separated list of proxy user logins allowed to login as a proxy user for every other user account.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Kerberos.html">Kerberos</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | kerberos.properties |

---
| __Key__ | java.security.auth.login.config |
|:----------------|:--------|
| __Description__ | Path to the Java authentication and authorization configuration file.<br> |
| __Default__ | /opt/open-xchange/etc/kerberosLogin.conf |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Kerberos.html">Kerberos</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | kerberos.properties |

---
| __Key__ | sun.security.krb5.debug |
|:----------------|:--------|
| __Description__ | Enable kerberos debugging.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Kerberos.html">Kerberos</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Logging.html">Logging</a> |
| __File__ | kerberos.properties |

---
| __Key__ | java.security.krb5.conf |
|:----------------|:--------|
| __Description__ | Path to the krb5.conf configuration file.<br> |
| __Default__ | /opt/open-xchange/etc/krb5.conf |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Kerberos.html">Kerberos</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | kerberos.properties |

---
