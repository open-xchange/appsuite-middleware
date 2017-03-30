---
title: Credential
---

This page shows all properties with the tag: Credential

| __Key__ | com.openexchange.rest.services.basic-auth.login |
|:----------------|:--------|
| __Description__ | Specify the user name used for HTTP basic auth by internal REST servlet<br>Both settings need to be set in order to have basic auth enabled - "com.openexchange.rest.services.basic-auth.login" and "com.openexchange.rest.services.basic-auth.password"<br>Default is empty. Please change!<br> |
| __Default__ | empty |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.rest.services.basic-auth.password |
|:----------------|:--------|
| __Description__ | Specify the password used for HTTP basic auth by internal REST servlet<br>Both settings need to be set in order to have basic auth enabled - "com.openexchange.rest.services.basic-auth.login" and "com.openexchange.rest.services.basic-auth.password"<br>Default is empty. Please change!<br> |
| __Default__ | USER_INPUT |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.dovecot.doveadm.apiSecret |
|:----------------|:--------|
| __Description__ | Specifies the API secret to communicate with the Dovecot DoveAdm REST interface.<br> |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/REST.html">REST</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | doveadm.properties |

---
| __Key__ | com.openexchange.mail.masterPassword |
|:----------------|:--------|
| __Description__ | The master password for primary mail/transport server. Only takes effect when property<br>"com.openexchange.mail.passwordSource" is set to "global"<br> |
| __Default__ | secret |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | mail.properties |

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
| __Key__ | com.openexchange.userfeedback.smtp.username |
|:----------------|:--------|
| __Description__ | Default username for SMTP.<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Feedback.html">Feedback</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | userfeedbackmail.properties |

---
| __Key__ | com.openexchange.userfeedback.smtp.password |
|:----------------|:--------|
| __Description__ | Password for the provided username<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Feedback.html">Feedback</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | userfeedbackmail.properties |

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
| __Key__ | com.openexchange.noreply.login |
|:----------------|:--------|
| __Description__ | Specifies the login for the no-reply account.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/No-Reply.html">No-Reply</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | noreply.properties |

---
| __Key__ | com.openexchange.noreply.password |
|:----------------|:--------|
| __Description__ | Specifies the password for the no-reply account.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/No-Reply.html">No-Reply</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | noreply.properties |

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
| __Key__ | com.openexchange.client.onboarding.plist.pkcs12store.password |
|:----------------|:--------|
| __Description__ | The password of the pkcs12 keystore.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.client.onboarding.plist.pkcs12store.filename |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Apple.html">Apple</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/PList.html">PList</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | client-onboarding.properties |

---
| __Key__ | com.openexchange.http.grizzly.keystorePassword |
|:----------------|:--------|
| __Description__ | Password for keystore containing certificate for secure connections.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SSL.html">SSL</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.oauth.[serviceId].apiKey |
|:----------------|:--------|
| __Description__ | The api key of your [serviceId] application.<br><br>See com.openexchange.oauth.[serviceId] for a list of currently known service ids.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/OAuth.html">OAuth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Boxcom.html">Boxcom</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Dropbox.html">Dropbox</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Google.html">Google</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LinkedIn.html">LinkedIn</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/MS_Live_Connect.html">MS Live Connect</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Twitter.html">Twitter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Xing.html">Xing</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Yahoo.html">Yahoo</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |

---
| __Key__ | com.openexchange.oauth.[serviceId].apiSecret |
|:----------------|:--------|
| __Description__ | The api secret of your [serviceId] application.<br><br>See com.openexchange.oauth.[serviceId] for a list of currently known service ids.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/OAuth.html">OAuth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Boxcom.html">Boxcom</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Dropbox.html">Dropbox</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Google.html">Google</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LinkedIn.html">LinkedIn</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/MS_Live_Connect.html">MS Live Connect</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Twitter.html">Twitter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Xing.html">Xing</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Yahoo.html">Yahoo</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |

---
| __Key__ | com.openexchange.oauth.xing.consumerKey |
|:----------------|:--------|
| __Description__ | The consumer key (for upsell).<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/OAuth.html">OAuth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Xing.html">Xing</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |

---
| __Key__ | com.openexchange.oauth.xing.consumerSecret |
|:----------------|:--------|
| __Description__ | The consumer secret (for upsell)<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/OAuth.html">OAuth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Xing.html">Xing</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |

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
| __Key__ | com.openexchange.report.client.proxy.username |
|:----------------|:--------|
| __Description__ | The username that should be used to autherise on the proxy server.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.report.client.proxy.password |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Report.html">Report</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Proxy.html">Proxy</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | reportclient.properties |

---
| __Key__ | com.openexchange.report.client.proxy.password |
|:----------------|:--------|
| __Description__ | The password that should be used to autherise on the proxy server.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.report.client.proxy.username |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Report.html">Report</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Proxy.html">Proxy</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | reportclient.properties |

---
| __Key__ | com.openexchange.mail.filter.masterPassword |
|:----------------|:--------|
| __Description__ | The master password for mail/transport server. Only takes effect when property<br>"com.openexchange.mail.filter.passwordSource" is set to "global".<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mail.filter.passwordSource |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.net.ssl.custom.truststore.password |
|:----------------|:--------|
| __Description__ | Defines the password to access the custom truststore.<br> |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.net.ssl.custom.truststore.enabled, com.openexchange.net.ssl.custom.truststore.path |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SSL.html">SSL</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | ssl.properties |

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
| __Key__ | com.openexchange.sms.sipgate.username |
|:----------------|:--------|
| __Description__ | The sipgate username.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Sipgate.html">Sipgate</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMS.html">SMS</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | sipgate.properties |

---
| __Key__ | com.openexchange.sms.sipgate.password |
|:----------------|:--------|
| __Description__ | The sipgate password.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Sipgate.html">Sipgate</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMS.html">SMS</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | sipgate.properties |

---
| __Key__ | com.openexchange.secret.secretRandom |
|:----------------|:--------|
| __Description__ | The random secret token |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Secret.html">Secret</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | secret.properties |

---
| __Key__ | com.openexchange.custom.spamexperts.panel.admin_user |
|:----------------|:--------|
| __Description__ | This property defines the username which should be used as basic auth<br> |
| __Default__ | admin |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spamexperts.html">Spamexperts</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | spamexperts.properties |

---
| __Key__ | com.openexchange.custom.spamexperts.panel.admin_password |
|:----------------|:--------|
| __Description__ | This property defines the password which should be used as basic auth<br> |
| __Default__ | demo |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spamexperts.html">Spamexperts</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | spamexperts.properties |

---
| __Key__ | com.openexchange.custom.spamexperts.imapuser |
|:----------------|:--------|
| __Description__ | username to authenticate against the imap server<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spamexperts.html">Spamexperts</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | spamexperts.properties |

---
| __Key__ | com.openexchange.custom.spamexperts.imappassword |
|:----------------|:--------|
| __Description__ | password to authenticate against the imap server<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spamexperts.html">Spamexperts</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | spamexperts.properties |

---
| __Key__ | JMXLogin |
|:----------------|:--------|
| __Description__ | Define the JMX login for authentication.<br>Leaving this property empty means not to use authentication.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Management.html">Management</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | management.properties |

---
| __Key__ | JMXPassword |
|:----------------|:--------|
| __Description__ | Define the JMX password in SHA hashed version.<br>This property only has effect if property "JMXLogin" is set.<br><br>======================================================================<br>             Using Perl to generate the SHA hash<br>======================================================================<br><br>The following Perl command can be used to generate such a password:<br>(requires to install the Digest::SHA1 Perl module)<br><br>  perl -M'Digest::SHA1 qw(sha1_base64)' -e 'print sha1_base64("YOURSECRET")."=\n";'<br><br>NOTE:<br>Since Debian Wheezy and Ubuntu 12.04 the corresponding Perl module has been replaced with "Digest::SHA" (and "Digest::SHA1" is no longer maintained)<br><br>======================================================================<br>             Using ruby to generate the SHA hash<br>======================================================================<br><br>Alternatively, ruby can be used to generate the appropriate SHA1 hash:<br><br>  ruby -rdigest -e 'puts Digest::SHA1.base64digest("YOURSECRET")'<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | JMXLogin |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Management.html">Management</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | management.properties |

---
