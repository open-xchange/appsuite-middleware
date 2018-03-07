---
Title: Jolokia - OX munin scripts
---

# How to install Munin scripts for Open-Xchange

## Installation on OX App Suite

### Debian GNU/Linux 7.0 (valid until v7.8.2)

Add the following entry to /etc/apt/sources.list.d/open-xchange.list if not already present:

```
deb https://software.open-xchange.com/products/appsuite/7.8.2/backend/DebianWheezy/ /

\# if you have a valid maintenance subscription, please uncomment the 
\# following and add the ldb account data to the url so that the most recent
\# packages get installed
\# deb https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/7.8.2/backend/updates/DebianWheezy/ /
```	

and run 

```
$ apt-get update
$ apt-get install open-xchange-munin-scripts
```

### Debian GNU/Linux 8.0

Add the following entry to /etc/apt/sources.list.d/open-xchange.list if not already present:

```
deb https://software.open-xchange.com/products/appsuite/stable/backend/DebianJessie/ /

\# if you have a valid maintenance subscription, please uncomment the 
\# following and add the ldb account data to the url so that the most recent
\# packages get installed
\# deb https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/DebianJessie/ /
```	

and run 

```
$ apt-get update
$ apt-get install open-xchange-munin-scripts
```

### SUSE Linux Enterprise Server 11 (valid until v7.8.2)
Add the package repository using zypper if not already present:

```
$ zypper ar https://software.open-xchange.com/products/appsuite/7.8.2/backend/SLES11 ox
```

If you have a valid maintenance subscription, please run the following command and add the ldb account data to the url so that the most recent packages get installed:

```
$ zypper ar https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/7.8.2/backend/updates/SLES11 ox-updates
```

and run

```
$ zypper ref
$ zypper in open-xchange-munin-scripts
```

### SUSE Linux Enterprise Server 12

Add the package repository using zypper if not already present:
```
$ zypper ar https://software.open-xchange.com/products/appsuite/stable/backend/SLE_12 ox
```

If you have a valid maintenance subscription, please run the following command and add the ldb account data to the url so that the most recent packages get installed:

```
$ zypper ar https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/SLES11 ox-updates
```

and run

```
$ zypper ref
$ zypper in open-xchange-munin-scripts
```

### RedHat Enterprise Linux 6

Start a console and create a software repository file if not already present:

```
$ vim /etc/yum.repos.d/ox.repo

[ox]
name=Open-Xchange
baseurl=https://software.open-xchange.com/products/appsuite/stable/backend/RHEL6/
gpgkey=https://software.open-xchange.com/oxbuildkey.pub
enabled=1
gpgcheck=1
metadata_expire=0m

\# if you have a valid maintenance subscription, please uncomment the 
\# following and add the ldb account data to the url so that the most recent
\# packages get installed
\# [ox-updates]
\# name=Open-Xchange Updates
\# baseurl=https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/RHEL6/
\# gpgkey=https://software.open-xchange.com/oxbuildkey.pub
\# enabled=1
\# gpgcheck=1
\# metadata_expire=0m
```

and run

```
$ yum update
$ yum install open-xchange-munin-scripts
```

### RedHat Enterprise Linux 7

Start a console and create a software repository file if not already present:

```
$ vim /etc/yum.repos.d/ox.repo

[ox]
name=Open-Xchange
baseurl=https://software.open-xchange.com/products/appsuite/stable/backend/RHEL7/
gpgkey=https://software.open-xchange.com/oxbuildkey.pub
enabled=1
gpgcheck=1
metadata_expire=0m

\# if you have a valid maintenance subscription, please uncomment the 
\# following and add the ldb account data to the url so that the most recent
\# packages get installed
\# [ox-updates]
\# name=Open-Xchange Updates
\# baseurl=https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/RHEL7/
\# gpgkey=https://software.open-xchange.com/oxbuildkey.pub
\# enabled=1
\# gpgcheck=1
\# metadata_expire=0m
```

and run

```
$ yum update
$ yum install open-xchange-munin-scripts
```

### CentOS 6

Start a console and create a software repository file if not already present:

```
$ vim /etc/yum.repos.d/ox.repo

[ox]
name=Open-Xchange
baseurl=https://software.open-xchange.com/products/appsuite/stable/backend/RHEL6/
gpgkey=https://software.open-xchange.com/oxbuildkey.pub
enabled=1
gpgcheck=1
metadata_expire=0m

\# if you have a valid maintenance subscription, please uncomment the 
\# following and add the ldb account data to the url so that the most recent
\# packages get installed
\# [ox-updates]
\# name=Open-Xchange Updates
\# baseurl=https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/RHEL6/
\# gpgkey=https://software.open-xchange.com/oxbuildkey.pub
\# enabled=1
\# gpgcheck=1
\# metadata_expire=0m
```

and run

```
$ yum update
$ yum install open-xchange-munin-scripts
```

### CentOS 7

Start a console and create a software repository file if not already present:

```
$ vim /etc/yum.repos.d/ox.repo

[ox]
name=Open-Xchange
baseurl=https://software.open-xchange.com/products/appsuite/stable/backend/RHEL7/
gpgkey=https://software.open-xchange.com/oxbuildkey.pub
enabled=1
gpgcheck=1
metadata_expire=0m

\# if you have a valid maintenance subscription, please uncomment the 
\# following and add the ldb account data to the url so that the most recent
\# packages get installed
\# [ox-updates]
\# name=Open-Xchange Updates
\# baseurl=https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/RHEL7/
\# gpgkey=https://software.open-xchange.com/oxbuildkey.pub
\# enabled=1
\# gpgcheck=1
\# metadata_expire=0m
```

and run

```
$ yum update
$ yum install open-xchange-munin-scripts
```

### Additional installation 

Munin-scripts for Jolokia need an additional perl modules json and lwp, which are set inside the depenecies. Those should be installed automaticly.

If not, please install the following modules:

Debian: 
* libwww-perl
* libjson-perl

rpm
* perl-JSON
* perl-libwww-perl


# How to interact with Jolokia for Open-Xchange

Open-Xchange does support Jolokia as a remote JMX-Bridge over HTTP. By Version 7.4.0 ongoing, it is located inside Open-Xchange Bundle and configured by jolokia.properties. Additional information can be found at [jolokia.org](http://www.jolokia.org/). 
This has been done to get less overhead and speak with the corresponding JMX-beans directly.

## Set configuration by user id and context id

### jolokia.properties

| Key   											| Default value                 | Comment																												|
|:--------------------------------------------------|:------------------------------|:----------------------------------------------------------------------------------------------------------------------|
| [com.openexchange.jolokia.start](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.jolokia.start) | false | start switch for jolokia	|
| [com.openexchange.jolokia.servlet.name](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.jolokia.servlet.name) | /monitoring/jolokia | Under what servlet name jolokia will be published, please bear in mind that this should not be forwarded by apache and kept internal	|
| [com.openexchange.jolokia.user](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.jolokia.user)|| User used for authentication with HTTP Basic Authentication. If not given, Jolokia will not start!	|
| [com.openexchange.jolokia.password](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.jolokia.password) || Password used for authentification, if not set "secret" is used.|
| [com.openexchange.jolokia.restrict.to.localhost](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.jolokia.restrict.to.localhost) | true | This setting will restrict jolokia access to localhost. It is completly ignored when a jolokia-access.xml is present	|


Keep in mind that Jolokia will not start unless you set 

	com.openexchange.jolokia.start = true
	com.openexchange.jolokia.user = yourUser
	com.openexchange.jolokia.password = yourPassword
	
When using Munin-Scripts with Jolokia, this user and password also need to be changed.

## Configuration for Jolokia munin scripts
In addition to the server properties in ``jolokia.properties`` the munin scripts has to be configured too. 

```
vim /etc/munin/plugin-conf.d/ox
```

| Key   				| Default value           					| Comment														|
|:----------------------|:------------------------------------------|:--------------------------------------------------------------|
| env.oxJolokiaUrl		| http://localhost:8009/monitoring/jolokia  | Base url for jolokia											|
| env.oxJolokiaUser		| "changeMe!Now"							| User used for authentication with HTTP Basic Authentication.	|
| env.oxJolokiaPassword	| "s3cr3t!toBeChanged"						| Password used for authentification							|

**WARNING**: If ``env.oxJolokiaUser`` is not changed from its default value ``changeMe!Now`` , monitoring will not work as the user ``changeMe!Now`` is set to stop monitoring inside the munin scripts.
Both, ``env.oxJolokiaUser`` and ``env.oxJolokiaPassword`` need to be set to the same value as set inside ``jolokia.properties``.


## Configuring for OX Documents / Documentconverter

OX Documents and Documentconverter monitoring can used by installing the additional packages ``open-xchange-documents-monitoring``. The Documentconverter uses a different port to access monitoring data. The corresponding ``oxJolokiaURL`` is has to be configured with an entry in the settings.

```
[ox_documentconverter*]
env.oxJolokiaUrl http://localhost:8008/monitoring/jolokia
```

See this [article](http://oxpedia.org/wiki/index.php?title=AppSuite:DocumentsMonitoring) for more details.


## Supplementary notes for distributed munin clients

### Munin node

The default munin node configuration only allows connections from localhost, which means that munin master has to run on the same host. If there already is a munin master running, that master IP has to be added to the file /etc/munin/munin-node.conf on each node:

```
\# A list of addresses that are allowed to connect.  This must be a
\# regular expression, since Net::Server does not understand CIDR-style
\# network notation unless the perl module Net::CIDR is installed.  You
\# may repeat the allow line as many times as you'd like

allow ^127\.0\.0\.1$
```

### Munin master

Make sure the munin and apache packages are installed. If only localhost is going to be monitored, the default configuration is sufficient. Other munin nodes can be added in the ``/etc/munin/munin.conf`` file:

```
\# a simple host tree
[localhost.localdomain]
    address 127.0.0.1
    use_node_name yes
By default, the munin monitoring web page is only reached from localhost, other hosts or networks can be added in /etc/apache2/conf.d/munin:

Allow from localhost 127.0.0.0/8 ::1    # the default setting
Allow from 10.99.0.0/8                  # added network
```

The munin webpage is located at [http://yourhost_where_munin_is_running.org/munin](http://yourhost_where_munin_is_running.org/munin).


# Running Jolokia

As Jolokia represents a JMX-Interface it is highly recommended not to forward it to the internet!

This is by default set through the use of
 
	com.openexchange.jolokia.restrict.to.localhost = true

and can be changed by either setting it to false or providing a ``jolokia-access.xml`` inside ``/opt/open-xchange/etc/``.

For further information how to setup this file, [http://www.jolokia.org/reference/html/security.html](http://www.jolokia.org/reference/html/security.html) is a good start as all those settings are usable.

## Jolokia with Grizzly

When using Grizzly and munin scripts on the same machine, you can connect to jolokia directly with the servers address, e.g.: ``http://localhost:8009/monitoring/jolokia``. When connecting through another machine, a best practise is to use the same forwarding as described below.

# Example

For a more detailed example, see [Jolokia login counter HOWTO](https://oxpedia.org/wiki/index.php?title=Jolokia_LoginCounter_HOWTO)


