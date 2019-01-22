---
title: Microsoft OneDrive
classes: toc
icon: fa-windows
---

To setup the Microsoft OneDrive file store you have to install the package `open-xchange-file-storage-onedrive`.

# Required Permissions

The following Microsoft Graph Permissions are required to enable the OneDrive cloud storage.

 * [Files.Read](https://docs.microsoft.com/en-us/graph/permissions-reference#delegated-permissions-9)
 * [Files.Read.All](https://docs.microsoft.com/en-us/graph/permissions-reference#delegated-permissions-9)
 * [Files.Read.Selected](https://docs.microsoft.com/en-us/graph/permissions-reference#delegated-permissions-9)
 * [Files.ReadWrite](https://docs.microsoft.com/en-us/graph/permissions-reference#delegated-permissions-9)
 * [Files.ReadWrite.All](https://docs.microsoft.com/en-us/graph/permissions-reference#delegated-permissions-9)
 * [Files.ReadWrite.AppFolder](https://docs.microsoft.com/en-us/graph/permissions-reference#delegated-permissions-9)
 * [Files.ReadWrite.Selected](https://docs.microsoft.com/en-us/graph/permissions-reference#delegated-permissions-9)
 * [offline_access](https://docs.microsoft.com/en-us/graph/permissions-reference#openid-permissions)
 * [openid](https://docs.microsoft.com/en-us/graph/permissions-reference#openid-permissions)
 * [profile](https://docs.microsoft.com/en-us/graph/permissions-reference#openid-permissions)

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