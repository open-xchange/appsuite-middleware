---
title: OAuth 2.0
---

# Common Preparations

To enhance the middleware over OAuth client functionality and to be able to use applications (e.g. calendar/contact synchronisation or cloud storage functionality) from third party providers (such as Google or Dropbox) that allow user authentication via OAuth, you need to do some groundwork first and prepare your nodes. The basic template for OAuth is:

 * Configure your nodes to be reachable via HTTPs
 * Install the `open-xchange-oauth` package
 * Register an App on the third party provider's website and generate key pairs
 * Configure your nodes to use those key pairs

For the last two steps there are explicity instructions for each supported OAuth provider depending on what you are trying to accomplish, e.g. configure Dropbox cloud storage, Google Calendars, etc.

## HTTPS

Your setup is required to be reachable via HTTPS, since the OAuth providers expect that a call-back URL to your setup is specified. Such a call-back URL is only accepted if it contains the `https://` scheme., e.g.:

 `https://my.oxsetup.invalid/ajax/defer`
 
## Keep HTTPS Protocol

The [Grizzly Cluster Setup](http://oxpedia.org/wiki/index.php?title=AppSuite:Grizzly#Cluster_setup) article shows that HTTPS communication is terminated by the Apache balancer in front of the Open-Xchange nodes. To let the Open-Xchange application know about the HTTPS protocol that is used to communicate with the Apache server:

* Either set a special header in the SSL virtual hosts configurations in Apache to forward this information. The de facto standard for this is the `X-Forwarded-Proto` header. See [this article](http://oxpedia.org/wiki/index.php?title=AppSuite:Grizzly#X-FORWARDED-PROTO_Header) on how to setup that header.
* Or force the Open-Xchange application to assume it is reached via SSL through setting property `com.openexchange.forceHTTPS=true` in the file `/opt/open-xchange/etc/server.properties`.

## Deferrer URL

Open-Xchange application uses the deferrer URL as call-back for some of the providers, which use OAuth v2.0 authentication (such as Google).

If your OX server is reachable only via one host name, you won't have to do anything. If it is reachable by more than one host name, create or open the file `/opt/openexchange/etc/deferrer.properties` and set the properties therein as such:

 `com.openexchange.http.deferrer.url=https://mymaindomain.invalid`

# Installation

Install via your OS's package manager the package `open-xchange-oauth`. This package provides all necessary authentication mechanisms for the following OAuth providers:

* Box
* Dropbox
* Flickr
* Google
* Microsoft
* Twitter
* Yahoo
* Xing
