---
title: Geolocation Deployment Guide
icon: fa-globe
tags: Geolocation, Configuration, Installation
---

# Installation on OX App Suite

The middleware utilises a geolocation database for performing country IP checks, namely that provided by [MaxMind](https://dev.maxmind.com/). These geolocation sources are also used for the [session management]({{site.baseurl}}/middleware/login_and_sessions/06_sessionmanagement.html) feature.

To enable country IP check, only one source is required to and can be installed at any given time.
The respective package name of the geolocation source is `open-xchange-geoip-maxmind`.

Note that the free version of that source requires an attribution for public use. Please check the respective website for further information.

To install one or the other source use your OS's package manager.

This guide assumes that the `/opt/open-xchange/sbin` location is in your `$PATH`.

# Initialising/Updating the Database

As of January 2nd 2019, MaxMind [discontinued](https://support.maxmind.com/geolite-legacy-discontinuation-notice/) the legacy GeoLite database (the one used in previous versions of the middleware). However, a free geolocation database is still available through their [GeoLite2 databases](https://dev.maxmind.com/geoip/geoip2/geolite2/), which is distributed under the [Creative Commons Attribution-ShareAlike 4.0 International License](http://creativecommons.org/licenses/by-sa/4.0/). Attribution is required for using this database.

To ease the initialisation and update process of the geolocation database, MaxMind provides a [command line tool](https://dev.maxmind.com/geoip/geoipupdate/). That tool can fetch the latest version of the geolocation database from the respective server. Note that the update command line tool MUST be installed and executed on EVERY middleware node.

Also note that when it comes to updating the database, the external source has the right to rate limit the amount of requests or even deactivate your key (if you have any). Update sparingly.

If you already have downloaded the MaxMind's database and you simply wish to use it, or if you are performing an offline installation, or if you have a paid subscription and you received an update, simply set the correct path of the database file with the `com.openexchange.geolocation.maxmind.databasePath` property.

# Configuration

To enable the OX Geolocation Service and the country code check for IP changes the following properties must be set:

```bash
com.openexchange.geolocation.maxmind.databasePath=/var/lib/maxmind/GeoLite2-City.mmdb 
```
Defines the location of the MaxMind database.

```bash
com.openexchange.IPCheck=false
```
Disables the built-in IP strict check which upon session validation of every request the client IP address is compared with the client IP address used for the login request. Setting this parameter to `false` will only log the different client IP addresses with debug level.

```bash
com.openexchange.ipcheck.mode=countrycode
```
Enables the country code IP checker. Upon session validation of every request if an IP change is observed then the country code of both IPs is validated to assert whether the current IP is still assigned to the same country as the previous IP of the same session. In other words, validates whether the client changed countries. If yes, then the session is invalidated otherwise the change is applied to the session.

# Monitoring
There are different metrics captured during the operation of the Country Code IP Checker Service. Two graphs are generated over a 5 minute interval, plotting the amount of accepted and denied IP changes. Each graph plots different metrics regarding the reason of accepted/denied IP changes.

The "Accepted IP Changes" graph plots the total amount of:

 * Accepted IP changes
 * Accepted IP changes due to a private IPv4 change
 * Accepted IP changes due to white listed IPs
 * Accepted IP changes that don't fall under any of the previous categories

The "Denied IP Changes" graph plots the total amount of:

 * Denied IP changes
 * Denied IP changes due to country change
 * Denied IP changes due to an exception

# Logging
Different levels of logging are involved in the CountryCodeIPChecker Service's logger `com.openexchange.ipcheck.countrycode`.

If the IP change of a session is either accepted or kicked due to any number of reasons, then there will be a log entry in DEBUG level indicating that. If any error happens during the acquisition of the GeoInformation of any IP, then that error is logged in ERROR level (the session will be kicked in that case).
