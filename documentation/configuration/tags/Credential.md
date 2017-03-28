---
title: Credential
---

This page shows all properties with the tag: Credential

| __Key__ | com.openexchange.dovecot.doveadm.apiSecret |
|:----------------|:--------|
| __Description__ | Specifies the API secret to communicate with the Dovecot DoveAdm REST interface.<br> |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/REST.html">REST</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | doveadm.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].userName |
|:----------------|:--------|
| __Description__ | Specifies the user name to use for authentication.<br>Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].tenantName |
|:----------------|:--------|
| __Description__ | Specifies the tenant name to use for authentication.<br>Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].authValue |
|:----------------|:--------|
| __Description__ | Specifies the authentication value to use for authentication against Identity API v2.0;<br>see (http://developer.openstack.org/api-ref-identity-v2.html)<br>Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | readProperty.1 |
|:----------------|:--------|
| __Description__ | The db user name.<br> |
| __Default__ | user=openexchange |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | configdb.properties |

---
| __Key__ | readProperty.2 |
|:----------------|:--------|
| __Description__ | The database password.<br> |
| __Default__ | password=secret |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | configdb.properties |

---
| __Key__ | writeProperty.2 |
|:----------------|:--------|
| __Description__ | The database password.<br> |
| __Default__ | password=secret |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Config_DB.html">Config DB</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | configdb.properties |

---
| __Key__ | com.openexchange.http.grizzly.keystorePassword |
|:----------------|:--------|
| __Description__ | Password for keystore containing certificate for secure connections.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SSL.html">SSL</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | grizzly.properties |

---
| __Key__ | bindDNPassword |
|:----------------|:--------|
| __Description__ | The password required for the bindDN.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LDAP.html">LDAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
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
| __Key__ | com.openexchange.drive.events.apn.ios.password |
|:----------------|:--------|
| __Description__ | Specifies the password used when creating the referenced keystore containing<br>the certificate of the iOS application. Note that blank or null passwords<br>are in violation of the PKCS #12 specifications. Required if<br>"com.openexchange.drive.events.apn.ios.enabled" is "true" and the package<br>containing the restricted drive components is not installed.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.drive.events.apn.ios.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Apple.html">Apple</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.events.apn.macos.password |
|:----------------|:--------|
| __Description__ | Specifies the password used when creating the referenced keystore containing<br>the certificate of the Mac OS application. Note that blank or null passwords<br>are in violation of the PKCS #12 specifications. Required if<br>"com.openexchange.drive.events.apn.macos.enabled" is "true" and the package<br>containing the restricted drive components is not installed.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.drive.events.apn.macos.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Apple.html">Apple</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.mail.autoconfig.http.proxy.login |
|:----------------|:--------|
| __Description__ | Specifies the login/username to use in case specified proxy in property "com.openexchange.mail.autoconfig.http.proxy"<br>requires authentication.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mail.autoconfig.http.proxy.password, com.openexchange.mail.autoconfig.http.proxy |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Autoconfig.html">Autoconfig</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | autoconfig.properties |

---
| __Key__ | com.openexchange.mail.autoconfig.http.proxy.password |
|:----------------|:--------|
| __Description__ | Specifies the password to use in case specified proxy in property "com.openexchange.mail.autoconfig.http.proxy"<br>requires authentication.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mail.autoconfig.http.proxy.login, com.openexchange.mail.autoconfig.http.proxy |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Autoconfig.html">Autoconfig</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | autoconfig.properties |

---
| __Key__ | com.openexchange.hazelcast.group.password |
|:----------------|:--------|
| __Description__ | The password used when joining the cluster. <br>Please change this value, and ensure it's equal on all nodes in the cluster!!!<br> |
| __Default__ | wtV6$VQk8#+3ds!a |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.filestore.s3.[filestoreID].encryption.rsa.password |
|:----------------|:--------|
| __Description__ | Specifies the password used when creating the referenced keystore containing<br>public-/private-key pair to use for encryption. Note that blank or null<br>passwords are in violation of the PKCS #12 specifications. Required if<br>"com.openexchange.filestore.s3.[filestoreID].encryption" is set to "rsa".<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | filestore-s3.properties |

---
| __Key__ | com.openexchange.caldav.push.apsd.password |
|:----------------|:--------|
| __Description__ | Specifies the password used when creating the referenced keystore containing the APNS certificate.  <br>Required if com.openexchange.caldav.push.apsd.enabled is "true".   <br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.caldav.push.apsd.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CalDAV.html">CalDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | caldav.properties |

---
| __Key__ | com.openexchange.carddav.push.apsd.password |
|:----------------|:--------|
| __Description__ | Specifies the password used when creating the referenced keystore containing the APNS certificate.  <br>Required if com.openexchange.carddav.push.apsd.enabled is "true".   <br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.carddav.push.apsd.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CardDAV.html">CardDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | carddav.properties |

---
| __Key__ | mail.[protocol].user |
|:----------------|:--------|
| __Description__ | The user name to use when connecting to mail servers using the specified protocol. Overrides the mail.user property.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.user |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.user |
|:----------------|:--------|
| __Description__ | Default user name for SMTP<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.user |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.user |
|:----------------|:--------|
| __Description__ | Default user name for POP3.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.user |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | javamail.properties |

---
