---
title: File Storages
classes: toc
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
 
## OS Repositories
 
### Debian GNU/Linux 8.0

Add the following entry to `/etc/apt/sources.list.d/open-xchange.list` if not already present:

```
deb https://software.open-xchange.com/products/appsuite/stable/backend/DebianJessie/ /

# if you have a valid maintenance subscription, please uncomment the 
# following and add the ldb account data to the url so that the most recent
# packages get installed
# deb https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/DebianJessie/ /
```

### Debian GNU/Linux 9.0

Add the following entry to `/etc/apt/sources.list.d/open-xchange.list` if not already present:

```
deb https://software.open-xchange.com/products/appsuite/stable/backend/DebianStretch/ /

# if you have a valid maintenance subscription, please uncomment the 
# following and add the ldb account data to the url so that the most recent
# packages get installed
# deb https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/DebianStretch/ /
```

### SUSE Linux Enterprise Server 12

Add the package repository using `zypper` if not already present:

```
$ zypper ar https://software.open-xchange.com/products/appsuite/stable/backend/SLE_12 ox
If you have a valid maintenance subscription, please run the following command and add the ldb account data to the url so that the most recent packages get installed:

$ zypper ar https://[CUSTOMERID:PASSWORD]@software.open-xchange.com/products/appsuite/stable/backend/updates/SLES11 ox-updates
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
 
# Box.com

To setup the Box.com file store you have to install the package `open-xchange-file-storage-boxcom`.

## Registering your app

* Sign in to [box Developers](https://developers.box.com/)
* Select **Create a Box Application**
* Select **Box Content**
* Hit **Configure your application**
* Enter *redirect_uri* (the deferrer URL; e.g. `https://my.oxsetup.invalid/ajax/defer`)
* Enable _Read and write all files and folders_

## Configuration

In addition you have to configure the following properties in file `/opt/open-xchange/etc/boxcomoauth.properties`:

* Enable the OAuth connector
   `com.openexchange.oauth.boxcom=true`


* Set the API key and secret
  `com.openexchange.oauth.boxcom.apiKey=REPLACE_THIS_WITH_YOUR_BOX_CLIENT_KEY`
  `com.openexchange.oauth.boxcom.apiSecret=REPLACE_THIS_WITH_YOUR_BOX_CLIENT_SECRET`


* Set the redirect URL
  `com.openexchange.oauth.boxcom.redirectUrl=REPLACE_THIS_WITH_YOUR_BOX_REDIRECT_URL`


You can define them system-wide or via the config cascade mechanism.

## Install on OX AppSuite

Refer to [OS Repositories](#os-repositories) on how to configure the software repository of your operating system.

### Debian GNU/Linux 8.0/9.0

```
$ apt-get update
$ apt-get install open-xchange-file-storage-boxcom
```

### SUSE Linux Enterprise Server 12

```
$ zypper ref
$ zypper in open-xchange-file-storage-boxcom
```

### RedHat Enterprise Linux 6/7 and CentOS 6/7

```
$ yum update
$ yum install open-xchange-file-storage-boxcom
```

# Dropbox

To setup the Dropbox file store you have to install the package `open-xchange-file-storage-dropbox`.

## Registering your app

* Log in to your Dropbox account [here](https://www.dropbox.com/login), and create your Dropbox app [here](https://www.dropbox.com/developers/apps/create).
* There are two options available creating an app, Drops-in App & Dropbox API App. Please select **Dropbox API** app and enter the name of your app.
* Go to [App Console](https://www.dropbox.com/developers/apps) and select your created app. Select settings tab to view the `APP_KEY` (App key) and `SECRET_KEY` (App secret) and to configure the redirect URI to your AppSuite platform under the Oauth2 section. All the other fields can keep their default value.
* Please ensure the following conditions are met for the redirect URI:
   * The redirect URI uses `https://` as protocol
   * The redirect URI follows the pattern: `https://` + `<host-name>` + `/ajax/defer`
   * E.g. `https://myappsuite.mydomain.invalid/ajax/defer`
   
## Configuration

In addition you have to configure the following properties in file `/opt/open-xchange/etc/dropboxoauth.properties`:

* Enable the OAuth connector to Dropbox OAuth:
  `com.openexchange.oauth.dropbox=true`
* Set the API key and secret:
  `com.openexchange.oauth.dropbox.apiKey=REPLACE_THIS_WITH_DROPBOX_APP_KEY`
  `com.openexchange.oauth.dropbox.apiSecret=REPLACE_THIS_WITH_DROPBOX_APP_SECRET`
* Set the redirect URL. Please ensure the use the same URL as specified in the Dropbox App:
  `com.openexchange.oauth.dropbox.redirectUrl=`
* Set the product ID of the registered Dropbox app:
  `com.openexchange.oauth.dropbox.productName=`

You can define them system-wide or via the config cascade mechanism.

## Install on OX App Suite

Refer to [OS Repositories](#os-repositories) on how to configure the software repository of your operating system.

### Debian GNU/Linux 8.0/9.0

```
$ apt-get update
$ apt-get install open-xchange-file-storage-dropbox
```

### SUSE Linux Enterprise Server 12

```
$ zypper ref
$ zypper in open-xchange-file-storage-dropbox
```

### RedHat Enterprise Linux 6/7 and CentOS 6/7

```
$ yum update
$ yum install open-xchange-file-storage-dropbox
```

# Google Drive

To setup the Google Drive file store you have to install the package `open-xchange-file-storage-googledrive`.

## Registering your app

* Sign in to [Google Developers Console](https://console.developers.google.com/) using your Google account
* Please follow [these](https://developers.google.com/identity/sign-in/web/devconsole-project) instructions to create a new project with a client ID, which is needed to call the sign-in API
* Enable the following APIs for your project
   * BigQuery API
   * Calendar API
   * Contacts API
   * Drive API
   * Drive SDK
   * Gmail API
   * Google Cloud SQL
   * Google Cloud Storage
   * Google Cloud Storage JSON API
* perform [Google's site verification](https://support.google.com/webmasters/answer/35179)
   * you can use any method listed by Google in general
   * in case our OXaaS offering is used the HTML tag and HTML file methods are not accessible but the DNS based approach is required
* [get your app verified by Google](https://documentation.open-xchange.com/7.10.1/middleware/components/oauth/Google%20App%20Verification.html) to avoid awkward warnings.

## Configuration

In addition you have to configure the following properties in file `/opt/open-xchange/etc/googleoauth.properties`:

* Enable the OAuth connector to Google OAuth:
  `com.openexchange.oauth.google=true`
* Set the API key and secret, which is Client ID and Client Secret to call the sign-in API (Select your project, select API manager from upper left burger menu, select credentials in left side bar, select Client ID for Web application):
   `com.openexchange.oauth.google.apiKey=REPLACE_THIS_WITH_YOUR_CLIENT_ID`
   `com.openexchange.oauth.google.apiSecret=REPLACE_THIS_WITH_YOUR_CLIENT_SECRET`
* Set the redirect URL. Please ensure the following conditions are met:
   * The redirect URL specified in the Google App needs to be the same as the one specified by this property.
   * The redirect URI uses "https://" as protocol
   * The redirect URI follows the pattern: "https://" + <host-name> + "/ajax/defer"
     `com.openexchange.oauth.google.redirectUrl=`
      E.g. "https://myappsuite.mydomain.invalid/ajax/defer" 
* Set the product ID of the registered Google app
  `com.openexchange.oauth.google.productName=`


You can define them system-wide or via the config cascade mechanism.

## Install on OX App Suite

Refer to [OS Repositories](#os-repositories) on how to configure the software repository of your operating system.

### Debian GNU/Linux 8.0/9.0

```
$ apt-get update
$ apt-get install open-xchange-file-storage-googledrive
```

### SUSE Linux Enterprise Server 12

```
$ zypper ref
$ zypper in open-xchange-file-storage-googledrive
```

### RedHat Enterprise Linux 6/7 and CentOS 6/7

```
$ yum update
$ yum install open-xchange-file-storage-googledrive
```

# Microsoft Onedrive

To setup the Microsoft OneDrive file store you have to install the package `open-xchange-file-storage-onedrive`.

## Registering your app

* Please follow [this guide](https://msdn.microsoft.com/en-us/library/ff751474.aspx) to create/register your app
* application ID maps to apiKey in OX properties
* create credentials and copy it to apiSecret
* choose "Web" as platform
* enter the redirect URL according to the instruction in msliveconnectoauth.properties
* enter profile data for your application

## Configuration

In addition you have to configure the following properties in file `/opt/open-xchange/etc/msliveconntectoauth.properties`:

* Enable the OAuth connector
  `com.openexchange.oauth.msliveconnect=true`
* Set the API key and secret
  `com.openexchange.oauth.msliveconnect.apiKey=REPLACE_THIS_WITH_YOUR_MS_LIVE_CONNECT_CLIENT_KEY`
  `com.openexchange.oauth.msliveconnect.apiSecret=REPLACE_THIS_WITH_YOUR_MS_LIVE_CONNECT_CLIENT_SECRET`
* Set the redirect URL
  `com.openexchange.oauth.msliveconnect.redirectUrl=REPLACE_THIS_WITH_YOUR_MS_LIVE_CONNECT_REDIRECT_URL`

You can define them system-wide or via the config cascade mechanism.

## Install on OX App Suite

Refer to [OS Repositories](#os-repositories) on how to configure the software repository of your operating system.

### Debian GNU/Linux 8.0/9.0

```
$ apt-get update
$ apt-get install open-xchange-file-storage-onedrive
```

### SUSE Linux Enterprise Server 12

```
$ zypper ref
$ zypper in open-xchange-file-storage-onedrive
```

### RedHat Enterprise Linux 6/7 and CentOS 6/7

```
$ yum update
$ yum install open-xchange-file-storage-onedrive
```