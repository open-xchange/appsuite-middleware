---
title: Dropbox
classes: toc
icon: fa-dropbox
---

To setup the Dropbox file store you have to install the package `open-xchange-file-storage-dropbox`.

# Registering your app

* Log in to your Dropbox account [here](https://www.dropbox.com/login), and create your Dropbox app [here](https://www.dropbox.com/developers/apps/create).
* There are two options available creating an app, Drops-in App & Dropbox API App. Please select **Dropbox API** app and enter the name of your app.
* Go to [App Console](https://www.dropbox.com/developers/apps) and select your created app. Select settings tab to view the `APP_KEY` (App key) and `SECRET_KEY` (App secret) and to configure the redirect URI to your AppSuite platform under the Oauth2 section. All the other fields can keep their default value.
* Please ensure the following conditions are met for the redirect URI:
   * The redirect URI uses `https://` as protocol
   * The redirect URI follows the pattern: `https://` + `<host-name>` + `/ajax/defer`
   * E.g. `https://myappsuite.mydomain.invalid/ajax/defer`
   
# Configuration

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