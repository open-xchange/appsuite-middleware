---
title: SSL
---

This page shows all properties with the tag: SSL

| __Key__ | com.openexchange.http.grizzly.hasSSLEnabled |
|:----------------|:--------|
| __Description__ | Enable secure network listener.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SSL.html">SSL</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.http.grizzly.enabledCipherSuites |
|:----------------|:--------|
| __Description__ | Comma-separated list of cipher suites that should be used for secure connections.<br>See https://www.openssl.org/docs/manmaster/apps/ciphers.html<br>No value means system-default.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SSL.html">SSL</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.http.grizzly.keystorePath |
|:----------------|:--------|
| __Description__ | Path to keystore containing certificate for secure connections.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SSL.html">SSL</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.http.grizzly.keystorePassword |
|:----------------|:--------|
| __Description__ | Password for keystore containing certificate for secure connections.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Grizzly.html">Grizzly</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SSL.html">SSL</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | grizzly.properties |

---
| __Key__ | com.openexchange.net.ssl.default.truststore.enabled |
|:----------------|:--------|
| __Description__ | Defines if the default truststore provided by the JVM should be used. These truststore contains the Application Server’s trusted certificates, including public keys for other entities. For a trusted certificate, the server has confirmed that the public key in the certificate belongs to the certificate’s owner. Trusted certificates generally include those of certification authorities (CAs).<br>The administrator is able to ignore the provided by setting the property to 'false'. If so a custom truststore should be provided. Have a look at 'com.openexchange.net.ssl.custom.truststore.enabled' for more details.<br> |
| __Default__ | true |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.net.ssl.custom.truststore.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SSL.html">SSL</a> |
| __File__ | ssl.properties |

---
| __Key__ | com.openexchange.net.ssl.custom.truststore.enabled |
|:----------------|:--------|
| __Description__ | Defines if the custom truststore should be used to retrieve trusted certificates. The custom truststore should contain a list of certificates that are defined to be trusted.<br>It is possible to define only one custom truststore. But it is of course possible to enable both, default and custom truststore to enhance the trusted certificates pool.<br>If you would like to use a custom truststore it has to be in JKS format.<br> |
| __Default__ | false |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.net.ssl.custom.truststore.path, com.openexchange.net.ssl.custom.truststore.password |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SSL.html">SSL</a> |
| __File__ | ssl.properties |

---
| __Key__ | com.openexchange.net.ssl.custom.truststore.path |
|:----------------|:--------|
| __Description__ | Defines the path (including the name of the file) to the custom truststore. <br> |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.net.ssl.custom.truststore.enabled, com.openexchange.net.ssl.custom.truststore.password |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SSL.html">SSL</a> |
| __File__ | ssl.properties |

---
| __Key__ | com.openexchange.net.ssl.custom.truststore.password |
|:----------------|:--------|
| __Description__ | Defines the password to access the custom truststore.<br> |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.net.ssl.custom.truststore.enabled, com.openexchange.net.ssl.custom.truststore.path |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SSL.html">SSL</a> |
| __File__ | ssl.properties |

---
| __Key__ | com.openexchange.net.ssl.hostname.verification.enabled |
|:----------------|:--------|
| __Description__ | Defines if the name of the host should be checked while SSL handshaking. If the host name verification fails a connection to the desired host cannot be established even if there is a valid certificate. A host name verifier ensures the host name in the URL to which the client connects matches the host name in the digital certificate that the server sends back as part of the SSL connection.  <br> |
| __Default__ | true |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SSL.html">SSL</a> |
| __File__ | ssl.properties |

---
| __Key__ | com.openexchange.net.ssl.trustlevel |
|:----------------|:--------|
| __Description__ | Defines which level of trust should be considered for potentially secure connections (e. g. https). The default value 'all' means that all certificates will be trusted and a SSLSocketFactory that does not check certificates (and host names) will be used. You can switch this setting to 'restricted' so that every certificate provided by the defined endpoint will be validated trusted.<br> |
| __Default__ | all |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SSL.html">SSL</a> |
| __File__ | ssl.properties |

---
| __Key__ | com.openexchange.net.ssl.protocols |
|:----------------|:--------|
| __Description__ | Defines the protocols that will become supported for SSL communication. If the server does not support one of the mentioned protocols the SSL handshake will fail.<br> |
| __Default__ | TLSv1, TLSv1.1, TLSv1.2 |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SSL.html">SSL</a> |
| __File__ | ssl.properties |

---
| __Key__ | com.openexchange.net.ssl.ciphersuites |
|:----------------|:--------|
| __Description__ | Defines the cipher suites that will become supported for SSL communication. If the server does not support one of the mentioned suites the SSL handshake will fail.<br>The named cipher suites refer to the identifiers of OpenJDK. Although an attempt is in place to find the matching ones on other vendors' JREs, it might be a good<br>idea to name the desired cipher suites to use according to vendor-specific identifiers.<br> |
| __Default__ | TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA, TLS_RSA_WITH_AES_128_CBC_SHA, TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDH_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_DSS_WITH_AES_128_CBC_SHA, TLS_EMPTY_RENEGOTIATION_INFO_SCSV |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SSL.html">SSL</a> |
| __File__ | ssl.properties |

---
| __Key__ | com.openexchange.net.ssl.whitelist |
|:----------------|:--------|
| __Description__ | Defines a comma separated list of hosts certificates shouldn't be checked for validity. The list can contain wildcards and ip ranges. In addition it is possible to define a list by host name, IPv4 or IPv6 address. An incoming host name will not be checked against its IP address, for instance connecting against 'imap.gmail.com' will be possible if '\*.gmail.com' is whitelisted but adding only the corresponding IP address entry '64.233.167.108' as whitelisted won't work.   <br> |
| __Default__ | 127.0.0.1-127.255.255.255,localhost |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SSL.html">SSL</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/White_List.html">White List</a> |
| __File__ | ssl.properties |

---
| __Key__ | com.openexchange.net.ssl.user.configuration.enabled |
|:----------------|:--------|
| __Description__ | Defines if the user is able to define a more unsecure trust level than it is defined globally the administrator. For instance if 'com.openexchange.net.ssl.trustlevel' is 'restricted' the user won't be able to use untrusted connections (invalid certificates provided by the endpoint). If 'com.openexchange.net.ssl.user.configuration.enabled' is 'true' the user will be able to define that he will use untrusted connections.<br> |
| __Default__ | false |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.net.ssl.trustlevel, JSLob: io.ox/core//trustAllConnections<br> |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SSL.html">SSL</a> |
| __File__ | ssl.properties |

---
