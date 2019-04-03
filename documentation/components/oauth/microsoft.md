---
title: Microsoft
classes: toc
icon: fa-windows
---

# Register your App

First things first. As with every OAuth provider, you will first need to register your App with Microsoft. You can do this as follows:

* Sign in to [Microsoft Application Registration Portal](https://apps.dev.microsoft.com) using your Microsoft account
* Click on "*Add an app*" to the right
* Enter a name for your application in the the *Application Name* field and click on "*Create*" button
* After the application is created you will have to generate a new password. Click on the "*Generate New Password*" button uynder the "*Application Secrets*" sub-section.
* A pop-up dialog will display your generated password. Make a note of that as it will be used later in the Configuration section.
* Under the "*Platforms*" sub-section, click on the "*Add Platform*" button and select "*Web*"
* Enter the redirect URL as advised [here]({{ site.baseurl }}/middleware/components/oauth.html#common-preparations).
* Under the "*Microsoft Graph Permissions*" you can enable the "*Delegated Permissions*" that are relevant for your project. The middleware currently supports functionality for:
   * [Microsoft OneDrive]({{ site.baseurl }}/middleware/components/drive_accounts/microsoft_onedrive.html) (Read/Write)
   * [Microsoft Contacts]({{ site.baseurl }}/middleware/components/contacts/microsoft.html) (Read Only)
* The following permissions are shared among both OneDrive and Contacts and should be enabled right away:
 * [offline_access](https://docs.microsoft.com/en-us/graph/permissions-reference#openid-permissions)
 * [openid](https://docs.microsoft.com/en-us/graph/permissions-reference#openid-permissions)
 * [profile](https://docs.microsoft.com/en-us/graph/permissions-reference#openid-permissions)

# Configuration

In addition you have to configure the following properties in file `/opt/open-xchange/etc/microsoftgraphauth.properties`:

* Enable the OAuth connector to Google OAuth:
  `com.openexchange.oauth.microsoft.graph=true`
* Set the API key and secret, which is are the "*Application Id*" and the password you generated earlier in the "*Register your App*" section, to call the sign-in API:
   `com.openexchange.oauth.microsoft.graph.apiKey=REPLACE_THIS_WITH_YOUR_CLIENT_ID`
   `com.openexchange.oauth.microsoft.graph.apiSecret=REPLACE_THIS_WITH_YOUR_CLIENT_SECRET`
* Set the redirect URL. Please ensure the following conditions are met:
   * The redirect URL specified in the Google App needs to be the same as the one specified by this property.
   * The redirect URI uses "https://" as protocol
   * The redirect URI follows the pattern: "https://" + \<host-name\> + "/ajax/defer"
     `com.openexchange.oauth.microsoft.graph.redirectUrl=`
      E.g. "https://myappsuite.mydomain.invalid/ajax/defer" 

You can define them system-wide or via the config cascade mechanism.

# Upgrade to Microsoft Graph API

Back in 2017 Microsoft [announced](https://developer.microsoft.com/en-us/office/blogs/outlook-rest-api-v1-0-office-365-discovery-and-live-connect-api-deprecation/) the deprecation of Live SDK and Live Connect APIs, urging API consumers to [migrate](https://docs.microsoft.com/en-us/onedrive/developer/rest-api/concepts/migrating-from-live-sdk) to their new Graph API. The deprecated APIs will no longer be available after **November 1st, 2018**.

Regarding specific module APIs within the Live SDK and Live Connect, the Contacts API functionality ceased to return user data on **December 1st, 2017** and the OneDrive REST API on **November 1st, 2018**.

Furthermore, the OAuth tokens obtained for the previously, now deprecated APIs, will not work with the new Graph API, hence all consumers are advised to generate new tokens via the [Microsoft Application Registration Portal](https://apps.dev.microsoft.com) for their Apps.