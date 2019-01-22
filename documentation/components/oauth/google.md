---
title: Google
classes: toc
icon: fa-google
---

# Register your App

First things first. As with every OAuth provider, you will first need to register your App with Google. You can do this as follows:

* Sign in to [Google Developers Console](https://console.developers.google.com/) using your Google account
* Please follow [these](https://developers.google.com/identity/sign-in/web/devconsole-project) instructions to create a new project with a client ID, which is needed to call the sign-in API
* Enable the APIs that are relevant for your project. The middleware currently supports functionality for:
   * [Google Drive]({{ site.baseurl }}/middleware/components/drive_accounts/google_drive.html) (Read/Write)
   * [Google Contacts]({{ site.baseurl }}/middleware/components/contacts/google_contacts.html) (Read Only)
   * [Google Calendars]({{ site.baseurl }}/middleware/components/calendar/google_calendar.html) (Read Only)
* The following permissions are shared among Google Drive, Contacts and Calendars and should be enabled right away:
  * Google Cloud SQL
  * Google Cloud Storage
  * Google Cloud Storage JSON API
  * [BigQuery API](https://developers.google.com/identity/protocols/googlescopes#bigqueryv2)
* Perform the [Google's site verification](https://support.google.com/webmasters/answer/35179)
   * You can use any method listed by Google in general
   * In case our OXaaS offering is used the HTML tag and HTML file methods are not accessible but the DNS based approach is required
* [Get your app verified by Google](https://documentation.open-xchange.com/7.10.1/middleware/components/oauth/Google%20App%20Verification.html) to avoid awkward warnings.

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
   * The redirect URI follows the pattern: "https://" + \<host-name\> + "/ajax/defer"
     `com.openexchange.oauth.google.redirectUrl=`
      E.g. "https://myappsuite.mydomain.invalid/ajax/defer" 
* Set the product ID of the registered Google app
  `com.openexchange.oauth.google.productName=`

You can define them system-wide or via the config cascade mechanism.

# Google App Verification

## Problem
Google has recently restricted the possibility to allow 3rd party developers to integrate their applications with Google services, see 
[this article](https://support.google.com/cloud/answer/7454865) for further information. In essence, a verification process was introduced that requires multiple 
steps to be taken. This change affects all Google integrations for OX App Suite:

* external mail accounts (if configured to use OAuth for Google)
* calendar
* address book
* OX Drive storage integration

Without verification, users will see a warning message instead of a login form in the authorization popups. While the warning itself can be ignored with some effort, 
also the number of users that might use a certain 3rd party app with Google gets limited.

![](google/Google_verification_error.jpg)

## Solution
The following is necessary to avoid the limitations: (also see [Google's documentation](https://developers.google.com/apps-script/guides/client-verification#requesting_verification))

* In [Google Developer Console](https://console.developers.google.com/), ensure that the OAuth consent screen settings include a valid homepage URL and a 
publicly available privacy policy URL below the same domain.
* In [Google Search Console](https://www.google.com/webmasters/tools/home), ensure that the homepage URL is a verified property of the Google account that 
is used to obtained the API keys.
* Submit a verification request at Google

You need to specify the following scopes in the verification form, depending on which functionality you do offer. 
Unfortunately, every change of your offered feature set or changes by Open-Xchange might require re-verification in the future:

* https://mail.google.com/ (Read, send, delete, and manage users' email)
* https://www.googleapis.com/auth/calendar.readonly (View users' calendars)
* https://www.googleapis.com/auth/contacts.readonly (View users' contacts)
* https://www.googleapis.com/auth/drive (View and manage the files in the users' Google Drive)
* https://www.googleapis.com/auth/userinfo.profile (View the users' basic profile info, used to retrieve the users' unique identifiers)

Also, you need to provide an explanation why your instance of OX App Suite requires these scopes. A possible statement could be:

```
 With [Product Name], we offer email, personal information management
 and a cloud storage solution. We want to offer our users the possibility
 to use their respective Google account data from within our web
 application.
 - Our app uses scope https://mail.google.com/ to obtain an access token
   to authenticate to Gmails IMAP and SMTP servers via the XOAUTH2 SASL
   mechanism. The app allows users to integrate any IMAP account and offers a
   fully-fledged webmailer on top of it.
 - Scope https://www.googleapis.com/auth/contacts.readonly is used by our app
   to let users import contacts from their Google address book. Imports are
   periodically refreshed. The contacts are then used to make find-as-you-type
   suggestions when composing emails, creating appointments, etc.
 - We request scope https://www.googleapis.com/auth/calendar.readonly to let
   users import their appointments from Google Calendar into our own calendar
   application. Imports are periodically refreshed. The appointments can be
   viewed from within our web frontend.
 - The app uses https://www.googleapis.com/auth/drive to allow users to browse
   their Google Drive and manage files and folders. They also can select Google
   Drive files as attachments for eMails that are composed with our webmail
   module.
 - The scope https://www.googleapis.com/auth/userinfo.profile is used to request
   the user's unique identifier to distinguish multiple Google accounts of the same
   user and offer the ability to add those in our web application.
```

It might happen that despite submitting the verification request, you get no positive answer even after weeks. In that case please answer to the submit confirmation mail 
and ask for guidance on how to proceed to get your app verified. Google seems to react to these eMails. In our case, we were asked to again explain the requested scopes 
in more detail. After doing that, the app was verified within a day and the warning screen was gone.
