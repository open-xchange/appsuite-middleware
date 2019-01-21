---
title: Drive Accounts
icon: fa-hdd-o 
---
# Common Preparations

This page shows how to setup external file storages via OAuth 2.0. For all of these file storages you have to install the package `open-xchange-oauth`, which provides the necessary authentication mechanisms.

Moreover your setup is required to be reachable via HTTPS, since the providers expect that a call-back URL to your setup is specified. Such a call-back URL is only accepted if it contains the `https://` scheme., e.g.:

 `https://my.oxsetup.invalid/ajax/defer`
 
## Keep HTTPS Protocol

The [Grizzly Cluster Setup](http://oxpedia.org/wiki/index.php?title=AppSuite:Grizzly#Cluster_setup) article shows that HTTPS communication is terminated by the Apache balancer in front of the Open-Xchange nodes. To let the Open-Xchange application know about the HTTPS protocol that is used to communicate with the Apache server:

* Either set a special header in the SSL virtual hosts configurations in Apache to forward this information. The de facto standard for this is the `X-Forwarded-Proto` header. See [this article](http://oxpedia.org/wiki/index.php?title=AppSuite:Grizzly#X-FORWARDED-PROTO_Header) on how to setup that header.
* Or force the Open-Xchange application to assume it is reached via SSL through setting property `com.openexchange.forceHTTPS=true` in the file `/opt/open-xchange/etc/server.properties`.

## Deferrer URL

Open-Xchange application uses the deferrer URL as call-back for some of the providers, which use OAuth v2.0 authentication (such as Google).

If your OX server is reachable only via one host name, you won't have to do anything. If it is reachable by more than one host name, create or open the file `/opt/openexchange/etc/deferrer.properties` and set the properties therein as such:

 `com.openexchange.http.deferrer.url=https://mymaindomain.invalid`


For further details, please choose a subtopic on the left.
