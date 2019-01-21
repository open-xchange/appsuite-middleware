---
title: Box
classes: toc
---

To setup the Box.com file store you have to install the package `open-xchange-file-storage-boxcom`.

# Registering your app

* Sign in to [box Developers](https://developers.box.com/)
* Select **Create a Box Application**
* Select **Box Content**
* Hit **Configure your application**
* Enter *redirect_uri* (the deferrer URL; e.g. `https://my.oxsetup.invalid/ajax/defer`)
* Enable _Read and write all files and folders_

# Configuration

In addition you have to configure the following properties in file `/opt/open-xchange/etc/boxcomoauth.properties`:

* Enable the OAuth connector
   `com.openexchange.oauth.boxcom=true`


* Set the API key and secret
  `com.openexchange.oauth.boxcom.apiKey=REPLACE_THIS_WITH_YOUR_BOX_CLIENT_KEY`
  `com.openexchange.oauth.boxcom.apiSecret=REPLACE_THIS_WITH_YOUR_BOX_CLIENT_SECRET`


* Set the redirect URL
  `com.openexchange.oauth.boxcom.redirectUrl=REPLACE_THIS_WITH_YOUR_BOX_REDIRECT_URL`


You can define them system-wide or via the config cascade mechanism.

