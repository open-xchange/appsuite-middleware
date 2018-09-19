---
title: Database connection encryption
---

# Introduction
This guide will help you to setup SSL for your database and configure it for your OX system. Due to the fact that different operating systems and databases are supported by OX the guide might not cover everything needed for a successful setup. Therefore, please use the references below if errors occur. Any suggestion on how to improve the guide are welcome.

## Further reading
* [MariaDB and SSL](https://www.cyberciti.biz/faq/how-to-setup-mariadb-ssl-and-secure-connections-from-clients/) 
* [Ubuntu and MySQL](https://www.digitalocean.com/community/tutorials/how-to-configure-ssl-tls-for-mysql-on-ubuntu-16-04)
* [MySQL with user privileges (german)](https://www.thomas-krenn.com/de/wiki/MySQL_Verbindungen_mit_SSL_verschl%C3%BCsseln)
* [Official MySQL guide for SSL setup (JDBC)](https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-reference-using-ssl.html)

# Configure your Database
First things first. Before we can configure the Middleware, which is acting as the client, we need to setup the database (or rather the server) to use SSL. Therefore we need certificates.

## Certificate generation
The database you use makes a huge difference on how you can generate SSL certificates. If you e.g. use the enterprise version of MySQL you can use the parameter 'auto_generate_certs' (see [MySQL system variables](https://dev.mysql.com/doc/refman/5.7/en/server-system-variables.html#sysvar_auto_generate_certs) for details) and don't need to do anything. For all other cases see the next chapters.

### MySQL (community edition)
The community edition (since 5.7) ships a tool called 'mysql_ssl_rsa_setup' (see [MySQL SSL setup](https://dev.mysql.com/doc/refman/5.7/en/mysql-ssl-rsa-setup.html) for details). This tool will generate all needed certificates in just one simple step. E.g.

```
mysql_ssl_rsa_setup --datadir=/etc/mysql/ssl --suffix OX
```
(Precondition is an existing dir "ssl" in "/etc/mysql")



If you don't have the tool, follow the steps below.

### Command line
Before we generate the certificates, create a subdirectory in your SQL folder. This will help you to keep track of your SQL installation. After this execute the following commands to generate (self signed)

* A CA certificate
* A server certificate
* A client certificate to later copy to the Middleware


Note: This commands use the 'rsa' command to build a private key that begins with `-----BEGIN RSA PRIVATE KEY-----`. This is needed for yaSSL. The library expects the key to begin in this manner and fails if it does not. Therefore the guide uses this approach to generate SSL keys.

```
mkdir /etc/mysql/ssl
cd /etc/mysql/ssl

# Generate CA cert and key
openssl genrsa 2048 > ca-key.pem
openssl req -new -x509 -nodes -days 1095 -key ca-key.pem -out ca-cert.pem


# Generate server cert and key
openssl req -newkey rsa:2048 -days 1095 -nodes -keyout server-key.pem -out server-req.pem
openssl rsa -in server-key.pem -out server-key.pem 
openssl x509 -req -in server-req.pem -days 1095 -CA ca-cert.pem -CAkey ca-key.pem -set_serial 01 -out server-cert.pem


# Generate client cert and key
openssl req -newkey rsa:2048 -days 1095 -nodes -keyout client-key.pem -out client-req.pem
openssl rsa -in client-key.pem -out client-key.pem 
openssl x509 -req -in client-req.pem -days 1095 -CA ca-cert.pem -CAkey ca-key.pem -set_serial 01 -out client-cert.pem


# Verify the certificates against the self signed CA
openssl verify -CAfile ca-cert.pem server-cert.pem client-cert.pem 

chown mysql:mysql *
```

The last command might not be necessary for all databases, but you need for some and is a point of failure. This command too should be run at the client after copying certificates to the client.

## Activate SSL in the configuration
This step is the one with the most diversity. E.g. Debian ships MariaDB binaries that where compiled to use yaSSL (or rather with the new name wolfSSL). Therefore the configuration to activate SSL is `ssl on`. If the binaries were compiled to use OpenSSL the configuration param would look like this `ssl-cipher=TLSv1.2`. On MySQl it might be `ssl=1` or just `ssl`. Therefore the configuration showed below might not be working for your system:


### MySQL
```
(From /etc/mysql/my.cnf) 
[mysqld]
...
 ssl
 ssl-ca=/etc/mysql/ssl/ca-cert.pem
 ssl-cert=/etc/mysql/ssl/server-cert.pem
 ssl-key=/etc/mysql/ssl/server-key.pem
```

### MariaDB with yaSSL
Note: yaSSL does only support TLS in version TLSv1 and TLSv1.1

```

(From /etc/mysql/mariadb.conf.d/50-server.cnf )
#
# * Security Features
#
# For generating SSL certificates you can use for example the GUI tool "tinyca".
#
 ssl-ca=/etc/mysql/ssl/ca-cert.pem
 ssl-cert=/etc/mysql/ssl/server-cert.pem
 ssl-key=/etc/mysql/ssl/server-key.pem


#
# Accept only connections using the latest and most secure TLS protocol version.
# ..when MariaDB is compiled with OpenSSL:
# ssl-cipher=TLSv1.2
# ..when MariaDB is compiled with YaSSL (default in Debian):
 ssl=on
```

Afterwards restart your database. E.g.

```
systemctl restart mysql
```

# Configure your OX
## Copy the certificates
Before we can configure the OX Middleware we need to copy some certificates. For the rest of the guide we assume the certificates were copied to `/opt/openexchange/etc/ssl/database`.

For a two way authentication we need 

* ca-cert.pem
* client-cert.pem
* client-key-pem

Note: You can generate the certificates on the client, too, but keep in mind to use the same CA certificate. If you don't, your connections will fail with a generic `CommunicationLinkFailure`.

### Test with local client
Before you configure the Middleware to use SSL you should test if the configuration. Therefore run e.g.:

```
mysql -uopenexchange -p -h10.50.0.170 --ssl-mode=VERIFY_CA --ssl-ca=/opt/openexchange/etc/ssl/database/ca-cert.pem --ssl-cert=/opt/openexchange/etc/ssl/database/client-cert.pem \
      --ssl-key=/opt/openexchange/etc/ssl/database/client-key.pem
```

It is possible to use a connection without client certificates, too:

```
$ mysql --protocol=tcp -h localhost -u root -psecret --ssl-ca=/etc/mysql/ssl/ca-cert.pem
```

## Set the certificates
### Create key stores
The JDBC connector uses Java KeyStore to manage the different certificates. Therefore we need to create those based on the CA and client certificates. Following commands will create 

* A truststore file for the CA certifacte
* A keystore file for the client certificate

```
# Change to dir beforehand (cd /opt/openexchange/etc/ssl)
keytool -importcert -alias MySQLCACert -file ca-cert.pem -keystore truststore -storepass changeit
openssl pkcs12 -export -in client-cert.pem -inkey client-key.pem -name "mysqlclient" -passout pass:changeit -out client-keystore.p12
keytool -importkeystore -srckeystore client-keystore.p12 -srcstoretype pkcs12 -srcstorepass changeit -destkeystore keystore -deststoretype JKS -deststorepass changeit


# Verify client certificate in key store
keytool -list -v -keystore keystore
```

### Set connector properties
To activate SSL you have to set a bunch of properties in the configuration file 'dbconnector.yaml'. For a two way authentication, the file should look like this

```

com.mysql.jdbc:
# The driver properties should be kept at their defaults
    useUnicode: true
    characterEncoding: UTF-8
    autoReconnect: false
    useServerPrepStmts: false
    useTimezone: true
    serverTimezone: UTC
    connectTimeout: 15000
    socketTimeout: 15000
# SSL/TLS
    useSSL: true
    requireSSL: true
    verifyServerCertificate: true
    # Deactivate if you just want a server authentication
    clientCertificateKeyStoreUrl: file:/opt/open-xchange/etc/ssl/keystore
    clientCertificateKeyStorePassword: changeit
    clientCertificateKeyStoreType: JKS
    trustCertificateKeyStoreUrl: file:/opt/open-xchange/etc/ssl/truststore
    trustCertificateKeyStorePassword: changeit
    trustCertificateKeyStoreType: JKS
```

In the step above we have created an PKCS#12 key store. This can be used instead of the JKS store, too:

```
 clientCertificateKeyStoreUrl: file:/opt/open-xchange/etc/ssl/client-keystore.p12
clientCertificateKeyStorePassword: changeit
clientCertificateKeyStoreType: PKCS12
```

Note: Properties for the `configdb` will be overwritten by the properties defined in `configdb.properties`!!

Note: Based on the underlying JDK there might be problems with the available TLS versions. Newer JDK versions might only use TLSv1.1 or higher and the configured database might only accept TLSv1.

For a full property overview please have a look at the official [documentation](https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-configuration-properties.html).


# Reload properties and certificates in production

The server is capable to reload the JDBC properties as well as the used certificates. To trigger such a rotation the command line tool "reloadConfiguration" is used.

A reload will clear all current unused instances of a 'Connection' held in the connection pools and update the properties used to spawn new connections with the JDBC client. Therefore all newly created connections after a reload will automatically run with the updated properties.
Connections that are still used will get marked deprecated, so that these connections will be replaced by updated once after they finished their work.

## Change JDBC properties

JDBC properties are stored within the file 'dbconnector.yaml' within the folder '/opt/open-xchange/etc/'. Just add, change or delete a property within the file, save it and afterwards trigger the reload. That's it. 

For SSL related JDBC properties have a look at [Command Options for Encrypted Connections](https://dev.mysql.com/doc/refman/5.7/en/encrypted-connection-options.html)


### Update configDB properties  

ConfigDB related properties are still saved within 'configdb.properties'. Those properties are only applied for connections to the configDB and can actually overwrite the general JDBC properties. With 7.10.1 the default set of properties from the 'configdb.properties' were changed. If you upgraded your system you might need to adjust the properties. Since 7.10.1 only following the properties should be set for configDB

* the driver class
* the URL
* the DB user
* the DB users password

All you need to do after editing and saving, is to trigger a 'reloadConfiguration' too. If you only changed configDB related properties, only connections to the configDB will be replaced and updated.

## Key Rotation (certificate updates)

### Client certificate rotation
To switch client certificates you can either change the URL to new key stores containing the new certificates or you can add those certificates to the existing key stores. 

#### Recreating the key stores
One way to update the client certificates is to simply create new key stores like explained above in the section 'Create key stores'. Once the new certificates are imported in the new key stores, you can change the URLs for those key stores in the 'dbconnector.yaml', see 'Set connector properties'. After saving the file the command line tool 'reloadconfiguration' needs to be run. 


#### Change existing key store
The other way to rotate client related certificates is to add the new certificates to the existing stores. Due to the fact that the JDBC driver reloads the key stores for each new created connection itself, new connections will be configured with those too. Afterwards, the old certificates can be removed with the command

```
keytool -delete -alias mysqlcacert -keystore truststore
```

Effectively, this means that after all connections reached 'maxLifeTime', they will be replaced by connections with updated certificates.

The disadvantage of this method is that you can't be sure that after 'maxLifeTime' passed, all connections will have updated certificates. Keep in mind that there is no explicit logic involved by the Open-Xchange server (yet).

Therefore we recommend that after the old certificates were removed from the key stores a 'reloadconfiguration' is run. The Open-Xchange server will discover the changes on the key stores, and then trigger an explicit update of the connections. Advantages of the explicit reload are

* all new created connections will be configured using the new certificates instantly
* all idle connections will be destroyed and replaced by new connections
* connections that are used at the moment of the reload will be destroyed immediately after the connection isn't used anymore


### Server certificate rotation

#### Preparation
Replacing certificates on the mysql server is not possible without downtimes. The certificates are linked to the configuration of the databases themselves and thus aren't reloadable. Therefore prepare well before switching the certificates to avoid bigger downtimes. Generate and verify new certificates beforehand. 

To switch certificates simply replace the paths to the old certificates with the path to the new certificate within your database configuration. Don't overwrite those certificates!

Note: Keep in mind that before you switch the CA, all clients need to be updated. Add the new CA certificate to the trust store and generate new client certificates for the key store.

#### Restart
Once preparation is done restart the database service. E.g.

```
systemclt restart mysql
```

Note: Don't forget to remove all old certificates from the trust and the key store of the client.


