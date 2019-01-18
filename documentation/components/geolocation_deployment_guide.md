---
title: Geolocation
classes: toc
icon: fa-globe
---

# Installation on OX App Suite

The middleware utilises two geolocation databases for performing country IP checks, namely those provided by [MaxMind](https://dev.maxmind.com/) and [IP2Location](https://lite.ip2location.com/).

To enable country IP check, only one source is required to and can be installed at any given time.
The respective package names of those geolocation sources are `open-xchange-geoip-maxmind` and `open-xchange-geoip-ip2location`.

Note that the free versions of both sources require attribution for public use. Please check their respective websites for further information.

To install one or the other source use your OS's package manager.

This guide assumes that the `/opt/open-xchange/sbin` location is in your `$PATH`.

# Initialising/Updating the Database

As of 7.10.2, the geolocation database is accommodated in the `global` database. Make sure that you have one registered and the middleware is properly configured to use one. If not, please refer to the documentation [here](https://oxpedia.org/wiki/index.php?title=AppSuite:CrossContextDatabase) before reading any further.

To ease the initialisation and update process of the geolocation database, each package comes with its own command line tool. That tool can fetch the latest version of the geolocation database from their respective servers and import the data sets to the database. An offline mode is also available, where the tools can be fed with already downloaded datasets.

Note that when it comes to updating the database, both external sources have the right to rate limit the amount of requests or even deactivate your key (if you have any). Update sparingly.

## IP2Location

IP2Location provides an open source version of their database which is free for personal or commercial use, though [attribution](https://lite.ip2location.com/terms-of-use) is required. If you wish to use that as your geolocation source, you need to create an account and get a download token from their [website](https://lite.ip2location.com/login). The download token you will receive, will be used for all further updates of your database.

Now, after you have installed the `open-xchange-geoip-ip2location` package and restarted the middleware node, and assuming you already have a global database registered, a new table will be created under your global database, namely the `ip2location` table. That table will be populated with the use of the `ip2location` command line tool.

To initialise/update the database issue the following command:

```bash
root@mw-node:~# ip2location -A oxadminmaster -P secret -u openexchange -a secret -l -t <YOUR-IP2LOCATION-TOKEN>
```
The `-A` and `-P` arguments provide the command line tool with the master admin and master password of the installation. The `-u` and `-a` arguments provide the database user name and password. The `-l` flag indicates that you wish to download the lite version of their database (DB9 by default). If you have a paid subscription, you can ommit that flag to receive the full version. The `-k` flag simply dictates to the command line tool that any temporary files that will be created during this process should be kept and not deleted afterwards. Those files include the compressed database that will be downloaded from the server as well as the contents the extracted contentes of the archive. 

An example output of the previous command is listed below:

```bash
Temporary files will be KEPT in /tmp.
License key OK;12/31/2019;January 2019;131337;DB9LITECSV
Downloading DB9LITECSV...
Database size: 45.9 MB.

 /tmp/IP2LOCATION-LITE-DB9.CSV.ZIP 100.00% [==================================================>]  45.9 MB
Extracting the archive '/tmp/IP2LOCATION-LITE-DB9.CSV.ZIP' in '/tmp'...
Extracting to '/tmp/LICENSE_LITE.TXT'...OK
Extracting to '/tmp/README_LITE.TXT'...OK
Extracting to '/tmp/IP2LOCATION-LITE-DB9.CSV'...OK
Using database file '/tmp/IP2LOCATION-LITE-DB9.CSV'.
Importing data to schema 'oxglobal_global' in table(s) 'ip2location'...OK.
```

If you have already downloaded the ip2location database and you simply wish to imported, or if you are performing an offline installation, you can still use the same command line tool.

```bash
root@mw-node:~# ip2location -A oxadminmaster -P secret -u openexchange -a secret -i /tmp/IP2LOCATION-LITE-DB9.CSV.ZIP
```
The `-i` argument provides the command line tool with the ZIP archive containing the geolocation database. You can also feed the command line tool with the raw uncompressed .CSV file (simply use the same option as with the ZIP archive, i.e. `-i`).

An example output of the previous command is listed below:

```bash
Extracting the archive '/tmp/IP2LOCATION-LITE-DB9.CSV.ZIP' in '/tmp'...
Extracting to '/tmp/LICENSE_LITE.TXT'...OK
Extracting to '/tmp/README_LITE.TXT'...OK
Extracting to '/tmp/IP2LOCATION-LITE-DB9.CSV'...OK
Using database file '/tmp/IP2LOCATION-LITE-DB9.CSV'.
Importing data to schema 'oxglobal_global' in table(s) 'ip2location'...OK.
```

## MaxMind

As of January 2nd 2019, MaxMind [discontinued](https://support.maxmind.com/geolite-legacy-discontinuation-notice/) the legacy GeoLite database (the one used in previous versions of the middleware). However, a free geolocation database is still available through their [GeoLite2 databases](https://dev.maxmind.com/geoip/geoip2/geolite2/), which is distributed under the [Creative Commons Attribution-ShareAlike 4.0 International License](http://creativecommons.org/licenses/by-sa/4.0/). Attribution is required for using this database.

Similar to the `ip2location` package, the `open-xchange-geoip-maxmind` package provides the `maxmind` command line tool with which the global database will be populated. After installing this package and restarting the middleware node, two new tables will be created in your global database, i.e. `ip_blocks` and `ip_locations`.

To initialise/update the database issue the following command:

```bash
root@mw-node:~# maxmind -A oxadminmaster -P secret -u openexchange -a secret -k
```
The `-A` and `-P` arguments provide the command line tool with the master admin and master password of the installation. The `-u` and `-a` arguments provide the database user name and password. The `-k` flag simply dictates to the command line tool that any temporary files that will be created during this process should be kept and not deleted afterwards. Those files include the compressed database that will be downloaded from the server as well as the contents the extracted contentes of the archive.

An example output of the previous command is listed below:

```bash
Temporary files will be KEPT in '/tmp'.
Downloading GeoLite2-City-CSV...
Database size: 40.2 MB.

 /tmp/GeoLite2-City-CSV_20190115.zip 100.00% [==================================================>]  40.2 MB
Extracting the archive '/tmp/GeoLite2-City-CSV_20190115.zip' in '/tmp'...
Extracting to '/tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Blocks-IPv6.csv'...OK
Extracting to '/tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Locations-ja.csv'...OK
Extracting to '/tmp/GeoLite2-City-CSV_20190115/COPYRIGHT.txt'...OK
Extracting to '/tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Locations-zh-CN.csv'...OK
Extracting to '/tmp/GeoLite2-City-CSV_20190115/README.txt'...OK
Extracting to '/tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Blocks-IPv4.csv'...OK
Extracting to '/tmp/GeoLite2-City-CSV_20190115/LICENSE.txt'...OK
Extracting to '/tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Locations-fr.csv'...OK
Extracting to '/tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Locations-ru.csv'...OK
Extracting to '/tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Locations-en.csv'...OK
Extracting to '/tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Locations-pt-BR.csv'...OK
Extracting to '/tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Locations-de.csv'...OK
Extracting to '/tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Locations-es.csv'...OK
Using database files '/tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Blocks-IPv4.csv' and '/tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Locations-en.csv'.
Importing data to schema 'oxglobal_global' in table(s) 'ip_blocks, ip_locations'...OK.
```

If you already have downloaded the ip2location database and you simply wish to imported, or if you are performing an offline installation, or if you have a paid subscription and you received an update, you can still use the same command line tool.

```bash
root@mw-node:~# maxmind -A oxadminmaster -P secret -u openexchange -a secret -k -z /tmp/GeoLite2-City-CSV_20190115.zip
```
The `-z` argument provides the command line tool with the ZIP archive containing the geolocation database.

An example output of the previous command is listed below:

```bash
Extracting the archive '/tmp/GeoLite2-City-CSV_20190115.zip' in '/tmp'...
Extracting to '/tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Blocks-IPv6.csv'...OK
Extracting to '/tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Locations-ja.csv'...OK
Extracting to '/tmp/GeoLite2-City-CSV_20190115/COPYRIGHT.txt'...OK
Extracting to '/tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Locations-zh-CN.csv'...OK
Extracting to '/tmp/GeoLite2-City-CSV_20190115/README.txt'...OK
Extracting to '/tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Blocks-IPv4.csv'...OK
Extracting to '/tmp/GeoLite2-City-CSV_20190115/LICENSE.txt'...OK
Extracting to '/tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Locations-fr.csv'...OK
Extracting to '/tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Locations-ru.csv'...OK
Extracting to '/tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Locations-en.csv'...OK
Extracting to '/tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Locations-pt-BR.csv'...OK
Extracting to '/tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Locations-de.csv'...OK
Extracting to '/tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Locations-es.csv'...OK
Using database files '/tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Blocks-IPv4.csv' and '/tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Locations-en.csv'.
Importing data to schema 'oxglobal_global' in table(s) 'ip_blocks, ip_locations'...OK.
```
You can also feed the command line tool with the raw .CSV file by using the `-b` and `-l` options and provide them with the `ip_blocks` and `ip_locations` CSV files respectively.

```bash
root@mw-node:~# maxmind -A oxadminmaster -P secret -u openexchange -a secret -b /tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Blocks-IPv4.csv -ly-Locations-en.csvy-CSV_20190115/GeoLite2-City
```
An example output of the previous command is listed below:

```bash
Using database files '/tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Blocks-IPv4.csv' and '/tmp/GeoLite2-City-CSV_20190115/GeoLite2-City-Locations-en.csv'.
Importing data to schema 'oxglobal_global' in table(s) 'ip_blocks, ip_locations'...OK.
```

# Configuration

To enable the OX Geolocation Service and the country code check for IP changes the following properties must be set:

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
