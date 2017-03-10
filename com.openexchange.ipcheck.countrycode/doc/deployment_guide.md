#Deployment Guide for the OX Geolocation Service and Country IP Check

##Installation on OX App Suite

###Debian GNU/Linux 8.0

Add the following entry to /etc/apt/sources.list.d/open-xchange.list if not already present:

```bash
deb https://software.open-xchange.com/products/appsuite/stable/backend/DebianJessie/ /

# if you have a valid maintenance subscription, please uncomment the 
# following and add the ldb account data to the url so that the most recent
# packages get installed
# deb https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/DebianJessie/ /
```

and then run

```bash
$ apt-get update
$ apt-get install open-xchange-geoip
```

###SUSE Linux Enterprise Server 12

Add the package repository using zypper if not already present:

```bash
$ zypper ar https://software.open-xchange.com/products/appsuite/stable/backend/SLE_12 ox
```

If you have a valid maintenance subscription, please run the following command and add the ldb account data to the url so that the most recent packages get installed:

```bash
$ zypper ar https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/SLES12 ox-updates
```

and run

```bash
$ zypper ref
$ zypper in open-xchange-geoip
```

###RedHat Enterprise Linux 6 / CentOS 6

Start a console and create a software repository file if not already present:

```bash
$ vim /etc/yum.repos.d/ox.repo

[ox]
name=Open-Xchange
baseurl=https://software.open-xchange.com/products/appsuite/stable/backend/RHEL6/
gpgkey=https://software.open-xchange.com/oxbuildkey.pub
enabled=1
gpgcheck=1
metadata_expire=0m

# if you have a valid maintenance subscription, please uncomment the 
# following and add the ldb account data to the url so that the most recent
# packages get installed
# [ox-updates]
# name=Open-Xchange Updates
# baseurl=https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/RHEL6/
# gpgkey=https://software.open-xchange.com/oxbuildkey.pub
# enabled=1
# gpgcheck=1
# metadata_expire=0m
```

and run

```bash
$ yum update
$ yum install open-xchange-geoip
```

###RedHat Enterprise Linux 7 / CentOS 7

Start a console and create a software repository file if not already present:

```bash
$ vim /etc/yum.repos.d/ox.repo

[ox]
name=Open-Xchange
baseurl=https://software.open-xchange.com/products/appsuite/stable/backend/RHEL7/
gpgkey=https://software.open-xchange.com/oxbuildkey.pub
enabled=1
gpgcheck=1
metadata_expire=0m

# if you have a valid maintenance subscription, please uncomment the 
# following and add the ldb account data to the url so that the most recent
# packages get installed
# [ox-updates]
# name=Open-Xchange Updates
# baseurl=https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/RHEL7/
# gpgkey=https://software.open-xchange.com/oxbuildkey.pub
# enabled=1
# gpgcheck=1
# metadata_expire=0m
```

and run

```bash
$ yum update
$ yum install open-xchange-geoip
```

## Configuration

To enable the OX Geolocation Service and the country code check for IP changes the following properties must be set:

```bash
com.openexchange.IPCheck=false
```
Disables the built-in IP strict check which upon session validation of every request the client IP address is compared with the client IP address used for the login request. Setting this parameter to `false` will only log the different client IP addresses with debug level.

```bash
com.openexchange.ipcheck.mode=countrycode
```
Enables the country code IP checker. Upon session validation of every request if an IP change is observed then the country code of both IPs is validated to assert whether the current IP is still assigned to the same country as the previous IP of the same session. In other words, validates whether the client changed countries. If yes, then the session is invalidated otherwise the change is applied to the session.

```bash
com.openexchange.geolocation.maxmind.databasePath=/path/of/geolite2-city.mmdb
```
Defines the source of the [GeoLite2 City MaxMind GeoDB](http://dev.maxmind.com/geoip/geoip2/geolite2/).

## Monitoring
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

## Logging
Different levels of logging are involved in the CountryCodeIPChecker Service's logger `com.openexchange.ipcheck.countrycode`.

If the IP change of a session is either accepted or kicked due to any number of reasons, then there will be a log entry in DEBUG level indicating that. If any error happens during the acquisition of the GeoInformation of any IP, then that error is logged in ERROR level (the session will be kicked in that case).
