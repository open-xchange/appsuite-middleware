---
title: Encryption for Hazelcast
---

# Overview
Hazelcast does support different ways of encryption. On the one hand, there is a basic symmetric encryption, while on the other hand, Hazelcast is able to use TLS/SSL. This guide will lead you to both setups. 

>Hazelcast can only use one encryption method at a time. If both are configured, TLS is preferred.

>Online key rotation is only possible when using TLS/SSL

>Hazelcast Community Edition does not support security features. Only Hazelcast Enterprise and Hazelcast Enterprise HD versions support security features.


# Enter your license key
To add your license key set the following property:

```
com.openexchange.hazelcast.licenseKey=VERSION#XNODES#ACTUAL_KEY
# or
com.openexchange.hazelcast.licenseKey=ACTUAL_KEY
```

# Symmetric Encryption
Symmetric encryption uses a shared secret to encrypt transmitted data between members at socket level. The secret key is calculated based on a password, a salt which is applied on the password and a iteration count on how often to apply the algorithm of choice. Algorithms for symmetric encryption are defined by Hazelcast and can be one of the following:

* DES/ECB/PKCS5Padding
* PBEWithMD5AndDES
* Blowfish
* DESede (alias TripleDES)

To enable symmetric encryption, some properties within the `hazelcast.properties` file need to be adjusted. Per default the related properties look like this

```
com.openexchange.hazelcast.network.symmetricEncryption=false
com.openexchange.hazelcast.network.symmetricEncryption.algorithm=PBEWithMD5AndDES
com.openexchange.hazelcast.network.symmetricEncryption.salt=X-k4nY-Y*v38f=dSJrr)
com.openexchange.hazelcast.network.symmetricEncryption.password=&3sFs<^6[cKbWDW#du9s
com.openexchange.hazelcast.network.symmetricEncryption.iterationCount=19
```

At least the property `com.openexchange.hazelcast.network.symmetricEncryption` must be set to `true`. Furthermore, it is recommended to adjust the values for `com.openexchange.hazelcast.network.symmetricEncryption.salt` and `com.openexchange.hazelcast.network.symmetricEncryption.password` with a unique and randomly generated string.

Properties for symmetric encryption must be the same on all members.


# TLS/SSL
The other encryption method Hazelcast supports is Transport Layer Security. Therefore, either the [hazelcast.xml](http://docs.hazelcast.org/docs/latest/manual/html-single/index.html#tls-ssl-for-hazelcast-members) can be adjusted, or the properties that were introduced with the Open-Xchange Server version 7.10.1. Those properties are:

```
com.openexchange.hazelcast.network.ssl
com.openexchange.hazelcast.network.ssl.protocols
com.openexchange.hazelcast.network.ssl.trustStore
com.openexchange.hazelcast.network.ssl.trustStorePassword
com.openexchange.hazelcast.network.ssl.trustManagerAlgorithm
com.openexchange.hazelcast.network.ssl.keyStore
com.openexchange.hazelcast.network.ssl.keyStorePassword
com.openexchange.hazelcast.network.ssl.keyManagerAlgorithm
```

## Configure

### Create key stores
Hazelcast uses a Java key store to manage the certificates. Therefore, we need to create those based on the CA and the certificates. The following commands will create 

* A truststore file for the CA certifacte
* A keystore file for the certificate

for one member in a Hazelcast cluster. 
 
```
keytool -importcert -alias CACert -file ca-cert.pem -keystore truststore -storepass changeit
openssl pkcs12 -export -in client-cert.pem -inkey client-key.pem -name "client" -passout pass:changeit -out client-keystore.p12
keytool -importkeystore -srckeystore client-keystore.p12 -srcstoretype pkcs12 -srcstorepass changeit -destkeystore keystore -deststoretype JKS -deststorepass changeit
```

For a mutual authentication (see next section), each certificate of each member needs to be added to the trust store 

```
keytool -importcert -alias memberCertificateXX -file member-certificateXX.pem -keystore truststore -storepass changeit
```

### Adjust properties
Hazelcast supports authentication on two levels. On the first level, each member identifies itself. The member has a public certificate that it provides for all other members. This is configured in the key store. To enable this level, you need to adjust the following properties:

```
com.openexchange.hazelcast.network.ssl=true
com.openexchange.hazelcast.network.ssl.keyStore=PATH_TO_YOUR_TRUST_STORE
com.openexchange.hazelcast.network.ssl.keyStorePassword=changeit
```

Furthermore, Hazelcast supports a higher level with mutual authentication. Each member knows each public certificate of the other members and can verify their identity. Therefore, you need to add each public certificate of all other cluster member to your local members trust store. So, each member must have at least
* n-1 client certificates, where n is the number of members in the cluster
* one CA certificate
in its trust store.

```
com.openexchange.hazelcast.network.ssl.trustStore=PATH_TO_YOUR_TRUST_STORE
com.openexchange.hazelcast.network.ssl.trustStorePassword=changeit
```


Additionally, the algorithm for the managing entity of each store can be adjusted. Those properties are backed up by lean configuration and have the standard values shown below.

```
com.openexchange.hazelcast.network.ssl.protocols=TLSv1,TLSv1.1,TLSv1.2
com.openexchange.hazelcast.network.ssl.trustManagerAlgorithm=SunX509
com.openexchange.hazelcast.network.ssl.keyManagerAlgorithm=SunX509
```

## Key Rotation
>This section describes how to update certificates for Hazelcast members on mutual authenticated connections.

Updating certificates for Hazelcast members isn't that easy. Each member must know each certificate of the other members. To avoid outages, the members should be updated one after the other.


## Update a member
After each of the following steps, the command line tool `reloadconfiguration` must be run either for the member itself (Member) or on all other members (Cluster).

1. (Member) To update a member, the new certificates have to be added to the key store. This works like explained above in `Create key stores`.
2. (Cluster) Once the member is updated you need to propagate the new certificate to each other member. Therefore, the new certificate needs to be added to each trust store of the other members. If the new certificate of the member was created by a new CA, don't forget to add the CA certificate to the trust store, too.
3. (Member) Now, all members know about the new certificate. But the old one is still valid. Therefore we have to remove the old certificate. To remove the certificate on the member with the new certificate run:

```
keytool -delete -alias clientcert -keystore keystore
```

4. (Cluster) After the old certificates were removed from the member itself, all other cluster members can remove the certificate too. Therefore, run on the other clusters members

```
keytool -delete -alias memberCertificateXX -keystore truststore
```

