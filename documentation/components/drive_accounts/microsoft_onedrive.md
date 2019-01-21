---
title: Microsoft OneDrive
classes: toc
icon: fa-windows
---

To setup the Microsoft OneDrive file store you have to install the package `open-xchange-file-storage-onedrive`.

# Registering your app

* Please follow [this guide](https://msdn.microsoft.com/en-us/library/ff751474.aspx) to create/register your app
* application ID maps to apiKey in OX properties
* create credentials and copy it to apiSecret
* choose "Web" as platform
* enter the redirect URL according to the instruction in msliveconnectoauth.properties
* enter profile data for your application

# Configuration

In addition you have to configure the following properties in file `/opt/open-xchange/etc/microsoftgraphoauth.properties`:

* Enable the OAuth connector
  `com.openexchange.oauth.microsoft.graph=true`
* Set the API key and secret
  `com.openexchange.oauth.microsoft.graph.apiKey=REPLACE_THIS_WITH_YOUR_MS_LIVE_CONNECT_CLIENT_KEY`
  `com.openexchange.oauth.microsoft.graph.apiSecret=REPLACE_THIS_WITH_YOUR_MS_LIVE_CONNECT_CLIENT_SECRET`
* Set the redirect URL
  `com.openexchange.oauth.microsoft.graph.redirectUrl=REPLACE_THIS_WITH_YOUR_MS_LIVE_CONNECT_REDIRECT_URL`

You can define them system-wide or via the config cascade mechanism.