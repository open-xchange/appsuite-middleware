---
title: Configure trusted TLS certificates
---

# Introduction
With v7.8.3 the Open-Xchange Server supports to specify a Java KeyStore containing the TLS certificates to trust when connections to external systems like external mail accounts, RSS feeds or OAuth shall be established. Additionally the desired protocols and/or cipher suites can also be configured.

# Installation
This feature is included in ``open-xchange-core`` package. Thus, no additional packages are required being installed.

# Configuration
Default with v7.8.3 is to trust all TLS certificates as before. It is possible to trust only certificates in JVM's default trust store. It is also possible to define a custom trust store to extend the set of trusted certificates or to specify the desired protocols and cipher suites or enable hostname verification. A whitelist can be used to maintain a list of hostnames and/or IP ranges to bypass certificate validation. The new properties are explained in the [configuration section](/components/middleware/config{{ site.baseurl }}/index.html#mode=features&feature=SSL).

# Quickstart

## Restrict trusted certificates to JVM's default trust store

```
# Restrict trusted certificates
com.openexchange.net.ssl.trustlevel=restricted
```

## Define a custom trust store
All certificates which should be trusted has to be put into a key store in Java KeyStore format. The custom trust store will extend the set of certificates from JVM's default trust store. If only custom trust store should be used, the default trust store has to be disabled. To add a custom trust store, the following properties has to be added to properties files (e.g. `ssl.properties`)

```   
# The path to the custom trust store
com.openexchange.net.ssl.custom.truststore.path=/path/to/truststore.jks
com.openexchange.net.ssl.custom.truststore.enabled=true
com.openexchange.net.ssl.custom.truststore.password=password
# Restrict trusted certificates
com.openexchange.net.ssl.trustlevel=restricted
```

## Specify a list of supported protocols
If supported protocols should be specified, the following property has to be added to properties files (e.g. `ssl.properties`)

```
# Defines the protocols that will become supported for SSL communication.
com.openexchange.net.ssl.protocols=TLSv1, TLSv1.1, TLSv1.2
```

## Specify a list of supported cipher suites
If supported cipher suites should be specified, the following property has to be added to properties files (e.g. `ssl.properties`)

```
# Defines the cipher suites that will become supported for SSL communication
com.openexchange.net.ssl.ciphersuites=TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA, TLS_RSA_WITH_AES_128_CBC_SHA, TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDH_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_DSS_WITH_AES_128_CBC_SHA, TLS_EMPTY_RENEGOTIATION_INFO_SCSV
```

**Note**: The named cipher suites refer to the identifiers of OpenJDK. Although an attempt is in place to find the matching ones on other vendors' JREs, it might be a good idea to name the desired cipher suites to use according to vendor-specific identifiers. For instance, the identifier is ``TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA`` for OpenJDK, but ``SSL_ECDHE_ECDSA_WITH_AES_128_CBC_SHA`` on IBM Java.

## Disable hostname verification
Hostname verification can be disabled by setting following property:  

```
# Disables the hostname verification
com.openexchange.net.ssl.hostname.verification.enabled=false
```

## Use a whitelist 
A whitelist of hostnames and/or IP ranges can be set by following property

```
# A comma-separated list of hosts certificates shouldn't be checked for validity. The list can contain wildcards and ip ranges.
com.openexchange.net.ssl.whitelist=127.0.0.1-127.255.255.255,localhost
```

## Enable user configuration
It is possible to let the user decide if he wants to trust a restricted set of certificates or to trust all certificates. If enabled, the user can choose to trust all certificates in his general settings.

```
# Defines if the user is able to define a more unsecure trust level than it is defined globally the administrator
com.openexchange.net.ssl.user.configuration.enabled=true
```

## Disable the JVM's default trust store
The JVM's default trust store can be disabled. In this case, a custom trust store must be configured.

```
# Disable the JVM's default trust store. It only makes sense, if a custom trust store is enabled
com.openexchange.net.ssl.default.truststore.enabled=false
```

# Known issues

## Mailaccount autoconfiguration
External mail accounts can be autoconfigured by using a database hosted by Mozilla Messaging. The certificate for domain ``live.mozillamessaging.com`` must be added to a custom trust store, if JVM's default trust store is disabled.

## Certificate incompatibility with Java 7
It might happen that handshaking fails and connections cannot be established due to incompatibility of cipher suites for the certificate in combination with Java 7. For instance running the Open-Xchange server on Java 7 and trying to connect to a server that provides a certificate with no support for any SHA-1 ciphers will result in a HandshakeException. 

## SNI extension bug with Java 8 (affected patched version <= 131)
Java 8 versions <= 1.8.0\_131 are affected by this [bug](https://bugs.openjdk.java.net/browse/JDK-8144566). In a nutshell the JDK does not send the `server_name` extension to the server, resulting in a request without the `Host` header set. As a consequence the server is unable to return the correct certificate with the appropriate common name and instead returns the default certificate configured for the host and it sets the default common name 'badssl-fallback-unknown-subdomain-or-no-sni', which obviously differs from the requested endpoint's hostname. 

To identify this problem you can simply set the JDK to log all SSL handshakes with the `-Djavax.net.debug=all` flag in the `ox-scriptconf.sh` under the `JAVA_OPTS_OTHER` property (more information on how to debug SSL/TLS connections you can find [here](https://docs.oracle.com/javase/7/docs/technotes/guides/security/jsse/ReadDebug.html)).

Upon starting the middleware server, you should see In the `open-xchange-console.log` some SSL information, i.e. initialisation of the trust store. For every connection the middleware is initiating and an SSL handshake is taking place, the relevant SSL certificates along with the entire SSL handshake process will be logged there. 

In case of the affected JDK version, the initial header the JDK is sending on behalf of the middleware server looks like this:

```
*** ClientHello, TLSv1.2
RandomCookie:  GMT: 1492186440 bytes = { 86, 206, 84, 37, 246, 139, 77, 249, 203, 236, 178, 93, 75, 65, 27, 134, 91, 24, 79, 38, 49, 98, 255, 93, 136, 11, 240, 128 }
Session ID:  {}
Cipher Suites: [TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA, TLS_RSA_WITH_AES_128_CBC_SHA, TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDH_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_DSS_WITH_AES_128_CBC_SHA, TLS_EMPTY_RENEGOTIATION_INFO_SCSV]
Compression Methods:  { 0 }
Extension elliptic_curves, curve names: {secp256r1, secp384r1, secp521r1, sect283k1, sect283r1, sect409k1, sect409r1, sect571k1, sect571r1, secp256k1}
Extension ec_point_formats, formats: [uncompressed]
Extension signature_algorithms, signature_algorithms: SHA512withECDSA, SHA512withRSA, SHA384withECDSA, SHA384withRSA, SHA256withECDSA, SHA256withRSA, SHA256withDSA, SHA224withECDSA, SHA224withRSA, SHA224withDSA, SHA1withECDSA, SHA1withRSA, SHA1withDSA
***
```

and the response it gets from the third party server along with the certificate chain looks like this:

```
*** ServerHello, TLSv1.2
RandomCookie:  GMT: -446688863 bytes = { 165, 40, 87, 204, 222, 66, 76, 63, 38, 18, 249, 199, 44, 216, 84, 154, 121, 167, 120, 1, 205, 91, 185, 241, 177, 72, 18, 239 }
Session ID:  {19, 239, 42, 64, 20, 153, 138, 237, 13, 62, 196, 164, 63, 16, 38, 47, 159, 234, 26, 133, 173, 159, 56, 205, 131, 150, 40, 200, 89, 253, 223, 232}
Cipher Suite: TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA
Compression Method: 0
Extension renegotiation_info, renegotiated_connection: <empty>
Extension ec_point_formats, formats: [uncompressed, ansiX962_compressed_prime, ansiX962_compressed_char2]
***
[... (omitted HEX content) ...]
*** Certificate chain
chain [0] = [
[
  Version: V3
  Subject: CN=badssl-fallback-unknown-subdomain-or-no-sni, O=BadSSL Fallback. Unknown subdomain or no SNI., L=San Francisco, ST=California, C=US
  Signature Algorithm: SHA256withRSA, OID = 1.2.840.113549.1.1.11

```

Note the value of the common name (CN) on the certificate chain.

The SSL handshake with a patched version that addresses the issue looks as follows:

```
*** ClientHello, TLSv1.2
RandomCookie:  GMT: 1492254068 bytes = { 158, 153, 60, 252, 173, 199, 154, 249, 218, 167, 190, 169, 176, 245, 78, 201, 253, 0, 165, 196, 19, 120, 1, 174, 50, 153, 128, 142 }
Session ID:  {}
Cipher Suites: [TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA, TLS_RSA_WITH_AES_128_CBC_SHA, TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDH_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_DSS_WITH_AES_128_CBC_SHA, TLS_EMPTY_RENEGOTIATION_INFO_SCSV]
Compression Methods:  { 0 }
Extension elliptic_curves, curve names: {secp256r1, secp384r1, secp521r1, sect283k1, sect283r1, sect409k1, sect409r1, sect571k1, sect571r1, secp256k1}
Extension ec_point_formats, formats: [uncompressed]
Extension signature_algorithms, signature_algorithms: SHA512withECDSA, SHA512withRSA, SHA384withECDSA, SHA384withRSA, SHA256withECDSA, SHA256withRSA, SHA256withDSA, SHA224withECDSA, SHA224withRSA, SHA224withDSA, SHA1withECDSA, SHA1withRSA, SHA1withDSA
Extension server_name, server_name: [type=host_name (0), value=expired.badssl.com]
***
```

and the response:

```
*** ServerHello, TLSv1.2
RandomCookie:  GMT: -771910274 bytes = { 124, 253, 250, 180, 59, 31, 252, 79, 203, 41, 53, 165, 122, 192, 55, 73, 36, 60, 86, 181, 139, 179, 110, 198, 110, 87, 114, 135 }
Session ID:  {177, 75, 88, 184, 223, 167, 226, 46, 109, 242, 84, 161, 219, 63, 248, 36, 244, 246, 216, 25, 2, 152, 154, 76, 163, 113, 68, 54, 220, 93, 135, 68}
Cipher Suite: TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA
Compression Method: 0
Extension server_name, server_name: 
Extension renegotiation_info, renegotiated_connection: <empty>
Extension ec_point_formats, formats: [uncompressed, ansiX962_compressed_prime, ansiX962_compressed_char2]
***
[... (omitted HEX content) ...]
*** Certificate chain
chain [0] = [
[
  Version: V3
  Subject: CN=*.badssl.com, OU=PositiveSSL Wildcard, OU=Domain Control Validated
  Signature Algorithm: SHA256withRSA, OID = 1.2.840.113549.1.1.11
```

The client is sending the required `server_name` extension to the server, and the server acknowledges this in the response. Consequently, the correct certificate is returned with the correct common name.

The bug is fixed with patch version 141 for [OpenJDK](https://bugs.openjdk.java.net/projects/JDK/versions/18709) and [Oracle JDK](http://www.oracle.com/technetwork/java/javase/2col/8u141-bugfixes-3720387.html). So, be sure to upgrade to the appropriate version.