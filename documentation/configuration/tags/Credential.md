---
title: Credential
---

This page shows all properties with the tag: Credential

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
| __Key__ | URL |
|:----------------|:--------|
| __Description__ | URL to the config interface. %c is replaced with context login information.<br>%u is replaced with user login information. %p is replaced with user password.<br> |
| __Default__ | http://localhost/?cid=%c&login=%u&pass=%p |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Configjump.html">Configjump</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | configjump.properties |

---
