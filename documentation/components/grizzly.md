---
title: Grizzly with SSL
---

This guide explains how to enable HTTPS in addition to HTTP for [Grizzly](https://javaee.github.io/grizzly/), the OX App Suite Middleware Servlet Container, using a self-signed certificate. The guide follows the simplified assumption, that Apache2 and Middleware run on the same host and only one instance of each process exists.

For basic setup of the Middleware and the Apache2 reverse proxy in front of it, please follow the according [quick installation guide](http://oxpedia.org/wiki/index.php?title=AppSuite:Main_Page_AppSuite#quickinstall) for your Linux distribution first.

# Create TLS Credentials

The following script creates a RSA 4096 bit keypair with a self-signed certificate that is valid for one year. The key pair and certificate are stored within a password-protected PKCS#12 keystore that is going to be used by the `open-xchange` daemon. Make sure the adjust parameters like algorithm, key strength, validity, certificate subject and keystore password as desired.

	#!/bin/bash -e

	# switch to tmp working dir
	rm -rf /tmp/tls
	mkdir -p /tmp/tls
	cd /tmp/tls
	
	# generate keypair and certificate
	openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365 -nodes \
		-subj "/C=DE/ST=NRW/L=Olpe/O=OX Software GmbH/OU=Engineering/CN=localhost"
	
	# create keystore with passord 'secret'
	openssl pkcs12 -export -name localhost -in cert.pem -inkey key.pem \
		-out grizzly-tls.p12 -passout pass:secret
	
	# move keystore to open-xchange config dir and set permissions
	chmod 0640 grizzly-tls.p12
	chown root:open-xchange grizzly-tls.p12
	mv grizzly-tls.p12 /opt/open-xchange/etc/
	
	# clean up
	rm -rf /tmp/tls


# Configure Grizzly

Adjust `/opt/open-xchange/etc/grizzly.properties`:

	### HTTPS
	################################################################################
	
	# Enable secure network listener
	# Default: false
	com.openexchange.http.grizzly.hasSSLEnabled=true
	
	# Comma-separated list of cipher suites that should be used for secure connections.
	# See https://www.openssl.org/docs/manmaster/apps/ciphers.html
	# No value means system-default.
	com.openexchange.http.grizzly.enabledCipherSuites=
	
	# Path to keystore containing certificate for secure connections
	com.openexchange.http.grizzly.keystorePath=/opt/open-xchange/etc/grizzly-tls.p12
	# Password for keystore containing certificate for secure connections
	com.openexchange.http.grizzly.keystorePassword=secret

Then restart `open-xchange`:

	$ systemctl restart open-xchange

When up again, the server will now listen on port `8010`in addition to `8009`, with `8010` accepting HTTPS connections only.


# Configure Apache2

At first `mod_ssl` needs to be anabled. On Debian this would be done with:

	$ a2enmod ssl

Then proxying to HTTPS backends needs to be enabled within the virtual host confguration. For example in `/etc/apache2/sites-enabled/000-default.conf`, add `SSLProxyEngine On` to the desired `VirtualHost` section(s). More settings to control the SSL proxying can be found [here](https://httpd.apache.org/docs/2.4/mod/mod_ssl.html). Example:

	<VirtualHost *:443>
	       ServerAdmin webmaster@localhost
	       SSLEngine On
	       SSLCertificateFile /path/to/certificate.crt
	       SSLCertificateKeyFile /path/to/private.key
	       RequestHeader set X-Forwarded-Proto "https"
	
	       SSLProxyEngine On
	
	       DocumentRoot /var/www/html
	       <Directory /var/www/html>
	               Options Indexes FollowSymLinks MultiViews
	               AllowOverride None
	               Order allow,deny
	               allow from all
	               RedirectMatch ^/$ /appsuite/
	       </Directory>
	
	       <Directory /var/www/html/appsuite>
	               Options None +SymLinksIfOwnerMatch
	               AllowOverride Indexes FileInfo
	       </Directory>
	</VirtualHost>

Last but not least the reverse proxy URIs need to adjusted. So in `/etc/apache2/conf-available/proxy_http.conf`, change all `BalancerMember`directives to have URI scheme `https` and target port `8010`. For example change:

	BalancerMember http://localhost:8009 timeout=100 smax=0 ttl=60 retry=60 loadfactor=50 route=APP1

to

	BalancerMember https://localhost:8010 timeout=100 smax=0 ttl=60 retry=60 loadfactor=50 route=APP1

Finally restart Apache2:

	$ systemctl restart apache2

