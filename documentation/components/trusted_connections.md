---
title: Configure trusted TLS certificates
---

# Introduction
With v7.8.3 the Open-Xchange Server supports to specify a Java KeyStore containing the TLS certificates to trust when connections to external systems like external mail accounts, RSS feeds or OAuth shall be established. Additionally the desired protocols and/or cipher suites can also be configured.

# Installation
This feature is included in ``open-xchange-core`` package. Thus, no additional packages are required being installed.

# Configuration
Default with v7.8.3 is to trust all TLS certificates as before. It is possible to trust only certificates in JVM's default trust store. It is also possible to define a custom trust store to extend the set of trusted certificates or to specify the desired protocols and cipher suites or enable hostname verification. A whitelist can be used to maintain a list of hostnames and/or IP ranges to bypass certificate validation. The new properties are explained in the [configuration section](https://documentation.open-xchange.com/latest/middleware/configuration/properties.html#SSL).

# Quickstart

## Restrict trusted certificates to JVM's default trust store

    # Restrict trusted certificates
    com.openexchange.net.ssl.trustlevel=restricted

## Define a custom trust store
All certificates which should be trusted has to be put into a key store in Java KeyStore format. The custom trust store will extend the set of certificates from JVM's default trust store. If only custom trust store should be used, the default trust store has to be disabled. To add a custom trust store, the following properties has to be added to properties files (e.g. `ssl.properties`)

    
    # The path to the custom trust store
    com.openexchange.net.ssl.custom.truststore.path=/path/to/truststore.jks
    com.openexchange.net.ssl.custom.truststore.enabled=true
    com.openexchange.net.ssl.custom.truststore.password=password
    # Restrict trusted certificates
    com.openexchange.net.ssl.trustlevel=restricted

## Specify a list of supported protocols
If supported protocols should be specified, the following property has to be added to properties files (e.g. `ssl.properties`)

    # Defines the protocols that will become supported for SSL communication.
    com.openexchange.net.ssl.protocols=TLSv1, TLSv1.1, TLSv1.2

## Specify a list of supported cipher suites
If supported cipher suites should be specified, the following property has to be added to properties files (e.g. `ssl.properties`)

    # Defines the cipher suites that will become supported for SSL communication
    com.openexchange.net.ssl.ciphersuites=TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA, TLS_RSA_WITH_AES_128_CBC_SHA, TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDH_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_DSS_WITH_AES_128_CBC_SHA, TLS_EMPTY_RENEGOTIATION_INFO_SCSV

## Disable hostname verification
Hostname verification can be disabled by setting following property 

    com.openexchange.net.ssl.hostname.verification.enabled=false

## Use a whitelist 
A whitelist of hostnames and/or IP ranges can be set by following property

    # A comma-separated list of hosts certificates shouldn't be checked for validity. The list can contain wildcards and ip ranges.
    com.openexchange.net.ssl.whitelist=127.0.0.1-127.255.255.255,localhost

## Enable user configuration
It is possible to let the user decide if he wants to trust a restricted set of certificates or to trust all certificates. If enabled, the user can choose to trust all certificates in his general settings.

    # Defines if the user is able to define a more unsecure trust level than it is defined globally the administrator
    com.openexchange.net.ssl.user.configuration.enabled=true

## Disable the JVM's default trust store
The JVM's default trust store can be disabled. In this case, a custom trust store must be configured.

    # Disable the JVM's default trust store. It only makes sense, if a custom trust store is enabled
    com.openexchange.net.ssl.default.truststore.enabled=false
