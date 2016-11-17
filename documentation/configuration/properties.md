---
title: Properties Overview
---

# Introduction

With release 7.8.3 Open-Xchange starts to use predefined values for properties stored in the code and to prevent adding the properties to files contained in /opt/open-xchange/etc and its folders below. The administrator is (of course) able to change the provided defaults by adding the property to the file of his choice (below /opt/open-xchange/etc). Because of this change this page describes (mainly) new properties that aren't visible  in a file but can be configured by the administrator.

All properties will be described by using the layout of the table below:

| Key 		  		  | `com.openexchange.foo` 	| 
| :---        		  | :---      					|
| Description 		  | Defines the foo 			|
| Default 			  | true						|
| Version 			  | 7.8.3						|
| Reloadable          | true						|
| Configcascade Aware | false						|
| Related 			  | `com.openexchange.bar` 	|
| File 			      | 							|

---

| Key 		  		  | `com.openexchange.bar` 	| 
| :---        		  | :---      					|
| Description 		  | Defines the bar 			|
| Default 			  | false						|
| Version 			  | 7.8.0						|
| Reloadable          | false						|
| Configcascade Aware | true						|
| Related 			  | `com.openexchange.foo` 	|
| File 			      | foobar.properties			|

These information are contained within the columns have the following meaning:

  * **Key**: The key of the property. This key has to be added to the file of the administrators choice to overwrite the default value.
  * **Description**: A short description of the property.
  * **Default**: The default value of the property (as defined within the code).
  * **Version**: The first version the property is available with.
  * **Reloadable**: Defines whether the value is reloadable or not.
  * **Configcascade Aware**: Defines whether the property is configcascade aware or not.
  * **Related**: Contains information about other properties that are related to the currently described one.
  * **File**: Describes the file where the property is defined. This column mainly exists for properties that have been available before 7.8.3 and are contained within a file.

## New properties

To insert a new property you just have to create or update the corresponding yml file in /documentation-generic/config folder.

The yml file must have the following structure:

array:
  - data:
      Key: c.o.some.property
      Description: >
        line1
        line2
        line3
      Default: true
      Version: 7.8.3
      Reloadable: true
      Configcascade_Aware: true
      Related: 
      File:
  - data:
      Key: c.o.some.property2
      Description: >
        line1
        line2
      Default: true
      Version: 7.8.0
      Reloadable: false
      Configcascade_Aware: false
      Related: c.o.some.property
      File: somefile.properties


If you would like to add a reference to another property use the following approach:

  * tag the destination property key by using `<a name="com.openexchange.foo">com.openexchange.foo</a>`
  * reference the tagged property by adding it to the 'related' column like `<a href="#com.openexchange.foo">com.openexchange.foo</a>`

# Properties



## Advertisement 

| Key | <span style="font-weight:normal">com.openexchange.advertisement.<reseller>.packageScheme</span> |
|:----------------|:--------|
| __Description__ |         Defines which package scheme is used for the reseller. <reseller> can be replaced with either the reseller name or the reseller id.<br>        Use 'OX_ALL' for the default reseller. Available package schemes are:<br>        Global - always uses the default reseller and default package.<br>        AccessCombinations - Using access combination names to retrive the package.<br>        TaxonomyTypes - Using taxonomy types to retrieve the package.<br> |
| __Default__ | Global  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | false  |
| __File__ | advertisement.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.advertisement.<reseller>.taxonomy.types</span> |
|:----------------|:--------|
| __Description__ |         Defines a comma separated list of taxonomy types which are used as package identifiers. <br>        This list is used by the 'TaxonomyTypes' package scheme to identify the package.<br> |
| __Default__ |  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | false  |
| __File__ | advertisement.properties  |

---


## Doveadm 

| Key | <span style="font-weight:normal">com.openexchange.dovecot.doveadm.enabled</span> |
|:----------------|:--------|
| __Description__ | Specifies whether the connector for the Dovecot DoveAdm REST interface will be enabled or not<br> |
| __Default__ | false  |
| __Version__ | 7.8.3  |
| __Reloadable__ | false  |
| __Configcascade Aware__ | false  |
| __File__ | doveadm.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.dovecot.doveadm.endpoints</span> |
|:----------------|:--------|
| __Description__ | Specifies the URIs to the Dovecot DoveAdm REST interface end-points. <br>e.g. "http://dovecot1.host.invalid:8081, http://dovecot2.host.invalid:8081, http://dovecot3.host.invalid:8081"<br><br>Moreover connection-related attributes are allowed to be specified to influence HTTP connection and pooling behavior<br>com.openexchange.dovecot.doveadm.endpoints.totalConnections        The number of total connections held in HTTP connection pool<br>com.openexchange.dovecot.doveadm.endpoints.maxConnectionsPerRoute  The number of connections per route held in HTTP connection pool; or less than/equal to 0 (zero) for auto-determining<br>com.openexchange.dovecot.doveadm.endpoints.readTimeout             The read time-out in milliseconds<br>com.openexchange.dovecot.doveadm.endpoints.connectTimeout          The connect time-out in milliseconds<br><br>Full example :<br>com.openexchange.dovecot.doveadm.endpoints=http://dovecot1.host.invalid:8081, http://dovecot2.host.invalid:8081<br>com.openexchange.dovecot.doveadm.endpoints.totalConnections=100<br>com.openexchange.dovecot.doveadm.endpoints.maxConnectionsPerRoute=0 (max. connections per route is then determined automatically by specified end-points)<br>com.openexchange.dovecot.doveadm.endpoints.readTimeout=2500<br>com.openexchange.dovecot.doveadm.endpoints.connectTimeout=1500<br><br>The values can be configured within a dedicated .properties file; e.g. 'doveadm.properties'.<br> |
| __Default__ |  |
| __Version__ | 7.8.3  |
| __Reloadable__ | false  |
| __Configcascade Aware__ | false  |
| __File__ | doveadm.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.dovecot.doveadm.apiSecret</span> |
|:----------------|:--------|
| __Description__ | Specifies the API secret to communicate with the Dovecot DoveAdm REST interface<br> |
| __Default__ |  |
| __Version__ | 7.8.3  |
| __Reloadable__ | false  |
| __Configcascade Aware__ | false  |
| __File__ | doveadm.properties  |

---


## Grizzly 

| Key | <span style="font-weight:normal">com.openexchange.http.grizzly.wsTimeoutMillis</span> |
|:----------------|:--------|
| __Description__ | Specifies the Web Socket timeout in milliseconds<br> |
| __Default__ | is 900000 (15 Minutes).  |
 |
| __Default__ | 900000  |
| __Version__ | 7.8.3  |
| __Reloadable__ | false  |
| __Configcascade Aware__ | false  |
| __Related__ | com.openexchange.websockets.enabled  |
| __File__ | grizzly.properties  |

---


## Mail 

| Key | <span style="font-weight:normal">com.openexchange.mail.useStaticDefaultFolders</span> |
|:----------------|:--------|
| __Description__ | Defines whether standard folder names should be initialized with the default values or not. <br>The default values can be configured within the Adminuser.properties file.<br> |
| __Default__ | false  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | true  |
| __File__ | mail.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.imap.setSpecialUseFlags</span> |
|:----------------|:--------|
| __Description__ | Defines whether the ox middleware is allowed to set special use flags.<br>If set to 'false' the ox middleware will never set any special use flags on folders on the imap server.<br>If set to 'true' the ox middleware will only set special use flags if no special use flag of that type exist yet.<br> |
| __Default__ | false  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | true  |
| __File__ | imap.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.mail.preferSentDate</span> |
|:----------------|:--------|
| __Description__ | Specifies what to consider as the date of a mail; either the internal received date or mail's sent date (as given by "Date" header).<br>This property is considered in case a client passes special "date" (661) column to "columns" parameter and/or "sort" parameter.<br> |
| __Default__ | false  |
| __Version__ | 7.8.3  |
| __Reloadable__ | false  |
| __Configcascade Aware__ | false  |
| __File__ | mail.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.mail.maxDriveAttachments</span> |
|:----------------|:--------|
| __Description__ | Specifies the max. number of Drive documents that are allowed to be sent via E-Mail<br> |
| __Default__ | 20  |
| __Version__ | 7.6.2  |
| __Reloadable__ | false  |
| __Configcascade Aware__ | false  |
| __File__ | mail.properties  |

---


## OAuth 

| Key | <span style="font-weight:normal">com.openexchange.oauth.modules.enabled.[oauth_provider]</span> |
|:----------------|:--------|
| __Description__ |         A comma seperated list of enabled oauth modules. <br>        This list can be configured for each individual oauth provider. <br>        To identify the oauth provider replace [oauth_provider] with the last part of the provider id.<br>        E.g. com.openexchange.oauth.google -> com.openexchange.oauth.modules.enabled.google<br>        Available modules are:<br>         -mail<br>         -calendar_ro<br>         -contacts_ro<br>         -calendar<br>         -contacts<br>         -drive<br>         -generic <br> |
| __Default__ | null  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | true  |
| __File__ | oauth.properties  |

---


## PushNotificationService 

| Key | <span style="font-weight:normal">com.openexchange.pns.delayDuration</span> |
|:----------------|:--------|
| __Description__ | The time in milliseconds a notification is queued in buffer to possible aggregate<br>with similar notifications that arrive during that time<br> |
| __Default__ | 1000  |
 |
| __Default__ | 1000  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | false  |
| __File__ | pns.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.pns.timerFrequency</span> |
|:----------------|:--------|
| __Description__ | The frequency/delay in milliseconds when the buffering queue will be checked for due<br>notifications (the ones exceeding delayDuration in queue)<br> |
| __Default__ | 500  |
 |
| __Default__ | 500  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | false  |
| __File__ | pns.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.pns.numProcessorThreads</span> |
|:----------------|:--------|
| __Description__ | Specifies the number of threads that concurrently handle due notifications that were transferred<br>from buffering queue to processing queue.<br> |
| __Default__ | 10  |
 |
| __Default__ | 500  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | false  |
| __File__ | pns.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.pns.maxProcessorTasks</span> |
|:----------------|:--------|
| __Description__ | Specifies the buffer size for due notifications that were transferred from buffering queue to<br>processing queue.<br> |
| __Default__ | 65536  |
 |
| __Default__ | 65536  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | false  |
| __File__ | pns.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.pns.transport.apn.ios.enabled</span> |
|:----------------|:--------|
| __Description__ | Specifies whether the APNS transport is enabled. That property is responsive to config-cascade<br>and reloadable as well.<br><br>Moreover, an even finer-grained decision is possible to be configured as a certain transport<br>is checked for availability providing user, context, client and topic.<br>Hence, it is possible to  specify:<br><br>com.openexchange.pns.transport.apn.ios.enabled + ("." + {client})? + ("." + {topic})?<br><br>com.openexchange.pns.transport.apn.ios.enabled.open-xchange-appsuite.ox:mail:new=true<br>com.openexchange.pns.transport.apn.ios.enabled.open-xchange-appsuite.ox:calendar:new=false<br><br>That allows the client "open-xchange-appsuite" (App Suite UI) to receive "new mail" notifications<br>via APNS, but not for "new appointment".<br><br> |
| __Default__ | false  |
 |
| __Default__ | false  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | true  |
| __File__ | pns.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.pns.transport.apn.ios.feedbackQueryInterval</span> |
|:----------------|:--------|
| __Description__ | Specifies the frequency in milliseconds when to query the Apple feedback service to check for expired<br>and/or invalid tokens.<br> |
| __Default__ | 3600000 (1 hour)  |
 |
| __Default__ | 3600000  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | false  |
| __File__ | pns.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.pns.transport.gcm.enabled</span> |
|:----------------|:--------|
| __Description__ | Specifies whether the GCM transport is enabled. That property is responsive to config-cascade<br>and reloadable as well.<br><br>Moreover, an even finer-grained decision is possible to be configured as a certain transport<br>is checked for availability providing user, context, client and topic.<br>Hence, it is possible to  specify:<br><br>com.openexchange.pns.transport.gcm.enabled + ("." + {client})? + ("." + {topic})?<br><br>com.openexchange.pns.transport.gcm.enabled.open-xchange-appsuite.ox:mail:new=true<br>com.openexchange.pns.transport.gcm.enabled.open-xchange-appsuite.ox:calendar:new=false<br><br>That allows the client "open-xchange-appsuite" (App Suite UI) to receive "new mail" notifications<br>via GCM, but not for "new appointment".<br><br> |
| __Default__ | false  |
 |
| __Default__ | false  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | true  |
| __File__ | pns.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.pns.transport.wns.enabled</span> |
|:----------------|:--------|
| __Description__ | Specifies whether the WNS transport is enabled. That property is responsive to config-cascade<br>and reloadable as well.<br><br>Moreover, an even finer-grained decision is possible to be configured as a certain transport<br>is checked for availability providing user, context, client and topic.<br>Hence, it is possible to  specify:<br><br>com.openexchange.pns.transport.wns.enabled + ("." + {client})? + ("." + {topic})?<br><br>com.openexchange.pns.transport.wns.enabled.open-xchange-appsuite.ox:mail:new=true<br>com.openexchange.pns.transport.wns.enabled.open-xchange-appsuite.ox:calendar:new=false<br><br>That allows the client "open-xchange-appsuite" (App Suite UI) to receive "new mail" notifications<br>via WNS, but not for "new appointment".<br><br> |
| __Default__ | false  |
 |
| __Default__ | false  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | true  |
| __File__ | pns.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.pns.transport.websocket.enabled</span> |
|:----------------|:--------|
| __Description__ | Specifies whether the Web Socket transport is enabled. That property is responsive to config-cascade<br>and reloadable as well.<br><br>Moreover, an even finer-grained decision is possible to be configured as a certain transport<br>is checked for availability providing user, context, client and topic.<br>Hence, it is possible to  specify:<br><br>com.openexchange.pns.transport.websocket.enabled + ("." + {client})? + ("." + {topic})?<br><br>com.openexchange.pns.transport.websocket.enabled.open-xchange-appsuite.ox:mail:new=true<br>com.openexchange.pns.transport.websocket.enabled.open-xchange-appsuite.ox:calendar:new=false<br><br>That allows the client "open-xchange-appsuite" (App Suite UI) to receive "new mail" notifications<br>via Web Socket, but not for "new appointment".<br> |
| __Default__ | true  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | true  |
| __Related__ | com.openexchange.websockets.enabled  |
| __File__ | pns.properties  |

---


## Report 

| Key | <span style="font-weight:normal">com.openexchange.report.appsuite.fileStorage</span> |
|:----------------|:--------|
| __Description__ | Reports filestorage directory for storage of report parts and composed data.<br> |
| __Default__ | /tmp  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | false  |
| __File__ | reportserialization.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.report.appsuite.maxChunkSize</span> |
|:----------------|:--------|
| __Description__ | Determines how many chunks of data can be kept in the report before saving them in the folder described in the <br>com.openexchange.report.client.fileStorage property<br> |
| __Default__ | 200  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | false  |
| __File__ | reportserialization.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.report.appsuite.maxThreadPoolSize</span> |
|:----------------|:--------|
| __Description__ | Number of threads allowed to work on the report at the same time.<br> |
| __Default__ | 20  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | false  |
| __File__ | reportserialization.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.report.appsuite.threadPriority</span> |
|:----------------|:--------|
| __Description__ | The priority that threads, working on the report have. Allowed value range is 1-10. 1 is the lowest, 10 the highest priority.<br> |
| __Default__ | 1  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | false  |
| __File__ | reportserialization.properties  |

---


## RSS 

| Key | <span style="font-weight:normal">com.openexchange.messaging.rss.feed.schemes</span> |
|:----------------|:--------|
| __Description__ |         Defines the URL schemes that are allowed while adding new RSS feeds. An empty value means all (by URL supported) schemes are allowed.<br> |
| __Default__ | http, https, ftp  |
| __Version__ | 7.8.3  |
| __Reloadable__ | false  |
| __Configcascade Aware__ | false  |
| __File__ | rssmessaging.properties  |

---


## SSL 

| Key | <span style="font-weight:normal">com.openexchange.net.ssl.default.truststore.enabled</span> |
|:----------------|:--------|
| __Description__ |         Defines if the default truststore provided by the JVM should be used. These truststore contains the Application Server’s trusted certificates, including public keys for other entities. For a trusted certificate, the server has confirmed that the public key in the certificate belongs to the certificate’s owner. Trusted certificates generally include those of certification authorities (CAs).<br>        The administrator is able to ignore the provided by setting the property to 'false'. If so a custom truststore should be provided. Have a look at 'com.openexchange.net.ssl.custom.truststore.enabled' for more details.<br> |
| __Default__ | true  |
| __Version__ | 7.8.3  |
| __Reloadable__ | false  |
| __Configcascade Aware__ | false  |
| __Related__ | com.openexchange.net.ssl.custom.truststore.enabled  |
| __File__ | ssl.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.net.ssl.custom.truststore.enabled</span> |
|:----------------|:--------|
| __Description__ |         Defines if the custom truststore should be used to retrieve trusted certificates. The custom truststore should contain a list of certificates that are defined to be trusted.<br>        It is possible to define only one custom truststore. But it is of course possible to enable both, default and custom truststore to enhance the trusted certificates pool.<br>        If you would like to use a custom truststore it has to be in JKS format.<br> |
| __Default__ | false  |
| __Version__ | 7.8.3  |
| __Reloadable__ | false  |
| __Configcascade Aware__ | false  |
| __Related__ | com.openexchange.net.ssl.custom.truststore.path, com.openexchange.net.ssl.custom.truststore.password  |
| __File__ | ssl.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.net.ssl.custom.truststore.path</span> |
|:----------------|:--------|
| __Description__ |         Defines the path (including the name of the file) to the custom truststore. <br> |
| __Default__ |  |
| __Version__ | 7.8.3  |
| __Reloadable__ | false  |
| __Configcascade Aware__ | false  |
| __Related__ | com.openexchange.net.ssl.custom.truststore.enabled, com.openexchange.net.ssl.custom.truststore.password  |
| __File__ | ssl.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.net.ssl.custom.truststore.password</span> |
|:----------------|:--------|
| __Description__ |         Defines the password to access the custom truststore.<br> |
| __Default__ |  |
| __Version__ | 7.8.3  |
| __Reloadable__ | false  |
| __Configcascade Aware__ | false  |
| __Related__ | com.openexchange.net.ssl.custom.truststore.enabled, com.openexchange.net.ssl.custom.truststore.path  |
| __File__ | ssl.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.net.ssl.hostname.verification.enabled</span> |
|:----------------|:--------|
| __Description__ |         Defines if the name of the host should be checked while SSL handshaking. If the host name verification fails a connection to the desired host cannot be established even if there is a valid certificate. A host name verifier ensures the host name in the URL to which the client connects matches the host name in the digital certificate that the server sends back as part of the SSL connection.  <br> |
| __Default__ | true  |
| __Version__ | 7.8.3  |
| __Reloadable__ | false  |
| __Configcascade Aware__ | false  |
| __File__ | ssl.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.net.ssl.trustlevel</span> |
|:----------------|:--------|
| __Description__ |         Defines which level of trust should be considered for potentially secure connections (e. g. https). The default value 'all' means that all certificates will be trusted and a SSLSocketFactory that does not check certificates (and host names) will be used. You can switch this setting to 'restricted' so that every certificate provided by the defined endpoint will be validated trusted.<br> |
| __Default__ | all  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | false  |
| __File__ | ssl.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.net.ssl.protocols</span> |
|:----------------|:--------|
| __Description__ |         Defines the protocols that will become supported for SSL communication. If the server does not support one of the mentioned protocols the SSL handshake will fail.<br> |
| __Default__ | TLSv1, TLSv1.1, TLSv1.2  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | false  |
| __File__ | ssl.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.net.ssl.ciphersuites</span> |
|:----------------|:--------|
| __Description__ |         Defines the cipher suites that will become supported for SSL communication. If the server does not support one of the mentioned suites the SSL handshake will fail.<br> |
| __Default__ | TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA, TLS_RSA_WITH_AES_128_CBC_SHA, TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDH_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_DSS_WITH_AES_128_CBC_SHA, TLS_EMPTY_RENEGOTIATION_INFO_SCSV  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | false  |
| __File__ | ssl.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.net.ssl.whitelist</span> |
|:----------------|:--------|
| __Description__ |         Defines a comma separated list of hosts certificates shouldn't be checked for validity. The list can contain wildcards and ip ranges. In addition it is possible to define a list by host name, IPv4 or IPv6 address. An incoming host name will not be checked against its IP address, for instance connecting against 'imap.gmail.com' will be possible if '*.gmail.com' is whitelisted but adding only the corresponding IP address entry '64.233.167.108' as whitelisted won't work.   <br> |
| __Default__ | 127.0.0.1-127.255.255.255,localhost  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | false  |
| __File__ | ssl.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.net.ssl.user.configuration.enabled</span> |
|:----------------|:--------|
| __Description__ |         Defines if the user is able to define a more unsecure trust level than it is defined globally the administrator. For instance if 'com.openexchange.net.ssl.trustlevel' is 'restricted' the user won't be able to use untrusted connections (invalid certificates provided by the endpoint). If 'com.openexchange.net.ssl.user.configuration.enabled' is 'true' the user will be able to define that he will use untrusted connections.<br> |
| __Default__ | false  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | true  |
| __Related__ | com.openexchange.net.ssl.trustlevel, JSLob: io.ox/core//trustAllConnections  |
| __File__ | ssl.properties  |

---


## Websockets 

| Key | <span style="font-weight:normal">com.openexchange.websockets.enabled</span> |
|:----------------|:--------|
| __Description__ | The main switch to enable/disable Web Sockets. That property is responsive to config-cascade<br>and reloadable as well.<br> |
| __Default__ | true  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | true  |
| __File__ | websockets.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.websockets.grizzly.remote.delayDuration</span> |
|:----------------|:--------|
| __Description__ | The time in milliseconds a message (that is supposed to be transferred to a remote cluster member)<br>is queued in buffer to await & aggregate equal messages that arrive during that time<br> |
| __Default__ | 1000  |
 |
| __Default__ | 1000  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | false  |
| __File__ | websockets.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.websockets.grizzly.remote.maxDelayDuration</span> |
|:----------------|:--------|
| __Description__ | The time in milliseconds a message (that is supposed to be transferred to a remote cluster member)<br>is at max. queued in buffer to await & aggregate equal messages that arrive during that time.<br>So, even if there was an equal message recently, message is flushed from queue to avoid holding back<br>a message forever in case there are frequent equal messages.<br> |
| __Default__ | 3000  |
 |
| __Default__ | 3000  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | false  |
| __File__ | websockets.properties  |

---
| Key | <span style="font-weight:normal">com.openexchange.websockets.grizzly.remote.timerFrequency</span> |
|:----------------|:--------|
| __Description__ | The frequency/delay in milliseconds when the buffering queue will be checked for due<br>"remote" messages (the ones exceeding delayDuration in queue)<br> |
| __Default__ | 500  |
 |
| __Default__ | 500  |
| __Version__ | 7.8.3  |
| __Reloadable__ | true  |
| __Configcascade Aware__ | false  |
| __File__ | websockets.properties  |

---
