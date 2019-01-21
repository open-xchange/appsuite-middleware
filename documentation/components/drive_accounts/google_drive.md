---
title: Google Drive
classes: toc
icon: fa-google
---
To setup the Google Drive file store you have to install the package `open-xchange-file-storage-googledrive`.

# Registering your app

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

# Configuration

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
