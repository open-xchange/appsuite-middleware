---
title: IMAP Auth
---

This page shows all properties with the tag: IMAP Auth

| __Key__ | IMAP_SERVER |
|:----------------|:--------|
| __Description__ | IMAP server ip or fqdn.<br> |
| __Default__ | localhost |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP_Auth.html">IMAP Auth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | imapauth.properties |

---
| __Key__ | IMAP_PORT |
|:----------------|:--------|
| __Description__ | Port on which the IMAP server is listening.<br> |
| __Default__ | 143 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP_Auth.html">IMAP Auth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | imapauth.properties |

---
| __Key__ | IMAP_USE_SECURE |
|:----------------|:--------|
| __Description__ | Set to true if connecting via imaps://<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP_Auth.html">IMAP Auth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Security.html">Security</a> |
| __File__ | imapauth.properties |

---
| __Key__ | IMAP_TIMEOUT |
|:----------------|:--------|
| __Description__ | Socket I/O timeout value in milliseconds.<br> |
| __Default__ | 5000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP_Auth.html">IMAP Auth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | imapauth.properties |

---
| __Key__ | IMAP_CONNECTIONTIMEOUT |
|:----------------|:--------|
| __Description__ | Socket connection timeout value in milliseconds.<br> |
| __Default__ | 5000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP_Auth.html">IMAP Auth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | imapauth.properties |

---
| __Key__ | USE_FULL_LOGIN_INFO |
|:----------------|:--------|
| __Description__ | Set to true to auth with "user@domain" instead of just "user" against imap server.<br>If true the "domain" part will be used as the context name of the ox system.<br>so add "domain" as a login mapping to be able to login.<br>If false, the plugin react as only 1 context exists in the ox system,<br>and this context has the mapping "defaultcontext" added.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP_Auth.html">IMAP Auth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imapauth.properties |

---
| __Key__ | USE_FULL_LOGIN_INFO_FOR_USER_LOOKUP |
|:----------------|:--------|
| __Description__ | Define if the internal user-name matches the full login string or just the user part;<br>meaning the user is supposed to be queried using "user@domain" instead of "user".<br><br>Note: This property is only effective if "USE_MULTIPLE" is set to "true"<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | USE_MULTIPLE |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP_Auth.html">IMAP Auth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imapauth.properties |

---
| __Key__ | USE_FULL_LOGIN_INFO_FOR_CONTEXT_LOOKUP |
|:----------------|:--------|
| __Description__ | Define if the internal context-name matches the full login string or just the domain part;<br>meaning the context is supposed to be queried using "user@domain" instead of "domain".<br><br>Note: This property is only effective if "USE_MULTIPLE" is set to "true"<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | USE_MULTIPLE |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP_Auth.html">IMAP Auth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imapauth.properties |

---
| __Key__ | LOWERCASE_FOR_CONTEXT_USER_LOOKUP |
|:----------------|:--------|
| __Description__ | Specifies whether user/context look-up is supposed to be performed<br>by lower-casing the utilized user/context information.<br><br>Note: This property is only effective if "USE_MULTIPLE" is set to "true".<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | USE_MULTIPLE |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP_Auth.html">IMAP Auth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imapauth.properties |

---
| __Key__ | USE_MULTIPLE |
|:----------------|:--------|
| __Description__ | If set to true the IMAP authentication plugin gets all IMAP server information from the OX database instead of reading<br>configuration from this file.<br>Following information is fetched from DB and will be used to authenticate against the server:<br>- server<br>- port<br>- ssl/tls<br>Moreover the proper login name is detected as configured by property "com.openexchange.mail.loginSource".<br>To use this feature, set the correct values while provisioning an OX user.<br>Useful if you have many IMAP Servers to connect to.<br><br>INFO: Domain part of the login is used as context name.<br>Example:<br>  test@test.org<br>  Username of the OX account must be "test" and name of the OX context must be "test.org".<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | LOWERCASE_FOR_CONTEXT_USER_LOOKUP, USE_FULL_LOGIN_INFO_FOR_CONTEXT_LOOKUP, USE_FULL_LOGIN_INFO_FOR_USER_LOOKUP |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP_Auth.html">IMAP Auth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imapauth.properties |

---
| __Key__ | com.openexchange.authentication.imap.imapAuthEnc |
|:----------------|:--------|
| __Description__ | Define the encoding for IMAP authentication.<br> |
| __Default__ | UTF-8 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP_Auth.html">IMAP Auth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imapauth.properties |

---
