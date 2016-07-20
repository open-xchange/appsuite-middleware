---
title: Grizzly with SSL
---

# Configure Grizzly with SSL
Use secure connections with grizzly on a single-node setup.

## Configure hostnames
To workaround a bug in Apache2.2, add alias localhost_tls for localhost to /etc/hosts

    127.0.0.1       localhost localhost_tls

## Create private key and certificate for grizzly
``openssl genrsa -aes256 -out server.key 1024``
``openssl req -x509 -sha256 -new -key server.key -out server.csr #use localhost_tls as common name!``
``openssl x509 -sha256 -days 3652 -in server.csr -signkey server.key -out selfsigned.crt``

## Export to PKCS#12 keystore and convert into jks keystore
``openssl pkcs12 -export -name localhost_tls -in selfsigned.crt -inkey server.key -out store.p12``
``keytool -importkeystore -destkeystore store.jks -srckeystore store.p12 -srcstoretype pkcs12 -alias localhost_tls``

## Create hashes and symlinks
``ln -s selfsigned.crt $(openssl x509 -hash -noout -in selfsigned.crt).0``

## Apache proxy_http configuration
Paste into ``/etc/apache/conf-available/proxy_http.conf`` when using Apache 2.4, ``/etc/apache/conf.d/proxy_http.conf`` when using Apache 2.2

    <IfModule mod_proxy_http.c>
      ProxyRequests Off
      ProxyStatus On
      # When enabled, this option will pass the Host: line from the incoming request to the proxied host.
      ProxyPreserveHost On
      # Please note that the servlet path to the soap API has changed:
      <Location /webservices>
        # restrict access to the soap provisioning API
        Order Deny,Allow
        Deny from all
        Allow from 127.0.0.1
        # you might add more ip addresses / networks here
        # Allow from 192.168 10 172.16
      </Location>
      # The old path is kept for compatibility reasons
      <Location /servlet/axis2/services>
        Order Deny,Allow
        Deny from all
        Allow from 127.0.0.1
      </Location>
      # Enable the balancer manager mentioned in
      # http://oxpedia.org/wiki/index.php?title=AppSuite:Running_a_cluster#Updating_a_Cluster
      <IfModule mod_status.c>
        <Location /balancer-manager>
          SetHandler balancer-manager
          Order Deny,Allow
          Deny from all
          Allow from 127.0.0.1
        </Location>
      </IfModule>
      <Proxy balancer://oxcluster>
        Order deny,allow
        Allow from all
        # multiple server setups need to have the hostname inserted instead localhost
        BalancerMember http://localhost:8009 timeout=100 smax=0 ttl=60 retry=60 loadfactor=50 route=OX0
        #BalancerMember http://10.242.2.6:8009 timeout=100 smax=0 ttl=60 retry=60 loadfactor=50 route=OX0
        # Enable and maybe add additional hosts running OX here
        # BalancerMember http://oxhost2:8009 timeout=100 smax=0 ttl=60 retry=60 loadfactor=50 route=OX2
        ProxySet stickysession=JSESSIONID|jsessionid scolonpathdelim=On
        SetEnv proxy-initial-not-pooled
        SetEnv proxy-sendchunked
      </Proxy> 
    </IfModule>

Paste into ``/etc/apache/conf-available/proxy_https.conf`` when using Apache 2.4, ``/etc/apache/conf.d/proxy_https.conf`` when using Apache 2.2

    <IfModule mod_proxy_http.c>
      ProxyRequests Off
      ProxyStatus On
      # When enabled, this option will pass the Host: line from the incoming request to the proxied host.
      ProxyPreserveHost On
      # Please note that the servlet path to the soap API has changed:
      <Location /webservices>
        # restrict access to the soap provisioning API
        Order Deny,Allow
        Deny from all
        Allow from 127.0.0.1
        # you might add more ip addresses / networks here
        # Allow from 192.168 10 172.16
      </Location>
      # The old path is kept for compatibility reasons
      <Location /servlet/axis2/services>
        Order Deny,Allow
        Deny from all
        Allow from 127.0.0.1
      </Location>
      # Enable the balancer manager mentioned in
      # http://oxpedia.org/wiki/index.php?title=AppSuite:Running_a_cluster#Updating_a_Cluster
      <IfModule mod_status.c>
        <Location /balancer-manager>
          SetHandler balancer-manager
          Order Deny,Allow
          Deny from all
          Allow from 127.0.0.1
        </Location>
      </IfModule>
      <Proxy balancer://oxcluster_tls>
        Order deny,allow
        Allow from all
        # multiple server setups need to have the hostname inserted instead localhost
        BalancerMember http://localhost:8010 timeout=100 smax=0 ttl=60 retry=60 loadfactor=50 route=OX0
        #BalancerMember http://10.242.2.6:8009 timeout=100 smax=0 ttl=60 retry=60 loadfactor=50 route=OX0
        # Enable and maybe add additional hosts running OX here
        # BalancerMember http://oxhost2:8009 timeout=100 smax=0 ttl=60 retry=60 loadfactor=50 route=OX2
        ProxySet stickysession=JSESSIONID|jsessionid scolonpathdelim=On
        SetEnv proxy-initial-not-pooled
        SetEnv proxy-sendchunked
      </Proxy> 
    </IfModule>

Enable configuration (Apache 2.4 only)
``a2enconf proxy_http proxy_https``

## Apache virtual hosts configuration
Configure virtual host for non-secure connections, paste into ``/etc/apache/sites-available/ox.conf``

    <VirtualHost *:80>
      ServerAdmin webmaster@localhost
      
      <Directory /var/www/html/appsuite>
        Options None +SymLinksIfOwnerMatch
        AllowOverride Indexes FileInfo
      </Directory>
      
      ProxyPass /ajax balancer://oxcluster/ajax
      ProxyPass /appsuite/api balancer://oxcluster/ajax
      ProxyPass /drive balancer://oxcluster/drive
      ProxyPass /infostore balancer://oxcluster/infostore
      ProxyPass /publications balancer://oxcluster/publications
      ProxyPass /realtime balancer://oxcluster/realtime
      ProxyPass /servlet balancer://oxcluster/servlet
      ProxyPass /webservices balancer://oxcluster/webservices
      
      RewriteEngine On
      RewriteCond %{HTTP_USER_AGENT}      Calendar           [OR]
      RewriteCond %{HTTP_USER_AGENT}      Reminders          [OR]
      RewriteCond %{HTTP_USER_AGENT}      DataAccess         [OR]
      RewriteCond %{HTTP_USER_AGENT}      DAVKit             [OR]
      RewriteCond %{HTTP_USER_AGENT}      Lightning          [OR]
      RewriteCond %{HTTP_USER_AGENT}      Adresboek          [OR]
      RewriteCond %{HTTP_USER_AGENT}      dataaccessd        [OR]
      RewriteCond %{HTTP_USER_AGENT}      Preferences        [OR]
      RewriteCond %{HTTP_USER_AGENT}      Adressbuch         [OR]
      RewriteCond %{HTTP_USER_AGENT}      AddressBook        [OR]
      RewriteCond %{HTTP_USER_AGENT}      Address\ Book      [OR]
      RewriteCond %{HTTP_USER_AGENT}      CalendarStore      [OR]
      RewriteCond %{HTTP_USER_AGENT}      CalendarAgent      [OR]
      RewriteCond %{HTTP_USER_AGENT}      accountsd          [OR]
      RewriteCond %{HTTP_USER_AGENT}      eM\ Client         [OR]
      RewriteCond %{HTTP_USER_AGENT}      CoreDAV
      RewriteRule (.*)                  http://localhost:8009/servlet/dav$1  [P]
    </VirtualHost>

Configure virtual host for secure connections, paste into ``/etc/apache/sites-available/ox-ssl.conf``

    <VirtualHost *:443>
      ServerAdmin webmaster@localhost
      SSLEngine On
      SSLCertificateFile /path/to/certificate.crt
      SSLCertificateKeyFile /path/to/private.key
      RequestHeader set X-Forwarded-Proto "https"
      SSLProxyEngine On
      SSLProxyCheckPeerCN on
      SSLProxyCheckPeerExpire on
      SSLProtocol all -SSLv2 -SSLv3
      SSLCipherSuite  ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384:ECDHE-ECDSA-AES256-GCM-SHA384:DHE-RSA-AES128-GCM-SHA256:DHE-DSS-AES128-GCM-SHA256:kEDH+AESGCM:ECDHE-RSA-AES128-SHA256:ECDHE-ECDSA-AES128-SHA256:ECDHE-RSA-AES128-SHA:ECDHE-ECDSA-AES128-SHA:ECDHE-RSA-AES256-SHA384:ECDHE-ECDSA-AES256-SHA384:ECDHE-RSA-AES256-SHA:ECDHE-ECDSA-AES256-SHA:DHE-RSA-AES128-SHA256:DHE-RSA-AES128-SHA:DHE-DSS-AES128-SHA256:DHE-RSA-AES256-SHA256:DHE-DSS-AES256-SHA:DHE-RSA-AES256-SHA:AES128-GCM-SHA256:AES256-GCM-SHA384:AES128-SHA256:AES256-SHA256:AES128-SHA:AES256-SHA:AES:CAMELLIA:DES-CBC3-SHA:!aNULL:!eNULL:!EXPORT:!DES:!RC4:!MD5:!PSK:!aECDH:!EDH-DSS-DES-CBC3-SHA:!EDH-RSA-DES-CBC3-SHA:!KRB5-DES-CBC3-SHA
      SSLHonorCipherOrder  on
      SSLProxyVerifyDepth 2
      SSLProxyCACertificatePath /etc/apache2/ssl/
      SSLProxyEngine On
      SSLProxyVerify require
      
      ProxyPass /ajax balancer://oxcluster_tls/ajax
      ProxyPass /appsuite/api balancer://oxcluster_tls/ajax
      ProxyPass /drive balancer://oxcluster_tls/drive
      ProxyPass /infostore balancer://oxcluster_tls/infostore
      ProxyPass /publications balancer://oxcluster_tls/publications
      ProxyPass /realtime balancer://oxcluster_tls/realtime
      ProxyPass /servlet balancer://oxcluster_tls/servlet
      ProxyPass /webservices balancer://oxcluster_tls/webservices
      
      <Directory /var/www/html/appsuite>
        Options None +SymLinksIfOwnerMatch
        AllowOverride Indexes FileInfo
      </Directory>
      
      RewriteEngine On
      RewriteCond %{HTTP_USER_AGENT}      Calendar           [OR]
      RewriteCond %{HTTP_USER_AGENT}      Reminders          [OR]
      RewriteCond %{HTTP_USER_AGENT}      DataAccess         [OR]
      RewriteCond %{HTTP_USER_AGENT}      DAVKit             [OR]
      RewriteCond %{HTTP_USER_AGENT}      Lightning          [OR]
      RewriteCond %{HTTP_USER_AGENT}      Adresboek          [OR]
      RewriteCond %{HTTP_USER_AGENT}      dataaccessd        [OR]
      RewriteCond %{HTTP_USER_AGENT}      Preferences        [OR]
      RewriteCond %{HTTP_USER_AGENT}      Adressbuch         [OR]
      RewriteCond %{HTTP_USER_AGENT}      AddressBook        [OR]
      RewriteCond %{HTTP_USER_AGENT}      Address\ Book      [OR]
      RewriteCond %{HTTP_USER_AGENT}      CalendarStore      [OR]
      RewriteCond %{HTTP_USER_AGENT}      CalendarAgent      [OR]
      RewriteCond %{HTTP_USER_AGENT}      accountsd          [OR]
      RewriteCond %{HTTP_USER_AGENT}      eM\ Client         [OR]
      RewriteCond %{HTTP_USER_AGENT}      CoreDAV
      RewriteRule (.*)                  https://localhost_tls:8010/servlet/dav$1        [P]
    </VirtualHost>

## Enable virtual hosts and ssl module
``a2ensite ox.conf ox-ssl.conf``
``a2enmod ssl``

Then restart apache
``service apache2 restart``

## Configure grizzly
Copy keystore into open-xchange configuration folder
``cp store.jks /opt/open-xchange/etc/store.jks```

Add to /opt/open-xchange/etc/grizzly.properties

    com.openexchange.http.grizzly.hasSSLEnabled=true
    com.openexchange.http.grizzly.keystorePath=/opt/open-xchange/etc/store.jks
    com.openexchange.http.grizzly.keystorePassword=secret

Then restart OX
``service open-xchange restart``
