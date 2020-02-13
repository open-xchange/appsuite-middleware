---
title: Microsoft
icon: fab fa-windows
tags: 3rd Party, Microsoft, Installation, Configuration, Filestore, Contacts
---

# Register your App

First things first. As with every OAuth provider, you will first need to register your App with Microsoft. You can do this as follows:

* Sign in to [Microsoft Azure Portal](https://portal.azure.com/) using your Microsoft account
* Choose App Registration
* Enter a name for your application in the the *Application Name* field
* Enter the redirect URL as advised [here](({{ site.baseurl }}/middleware/3rd_party_integrations.html#common-preparations) and click on "*Create*" button
* After the application is created you will have to generate a new password. Click on "*Certificates & secrets*" and click on "*New Client Secret*""
* Now you must enable required permission. Go to "*API Permissions*" and click on "*Add Permission*"
* Choose "*Microsoft Graph*" and then "*Delegated permissions*"
* Select the permissions that are relevant for your project. The middleware currently supports functionality for:
   * [Files](#microsoft-onedrive) (Read/Write)
   * [Contacts](#microsoft-contacts) (Read Only)
* The following permissions are shared among both Files and Contacts and should be enabled right away:
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

# Microsoft Contacts

## Required Permissions

The following Microsoft Graph Permissions are required to enable contact synchronisation:

 * [Contacts.Read](https://docs.microsoft.com/en-us/graph/permissions-reference#contacts-permissions)
 * [Contacts.Read.Shared](https://docs.microsoft.com/en-us/graph/permissions-reference#contacts-permissions)
 * [People.Read](https://docs.microsoft.com/en-us/graph/permissions-reference#people-permissions)
 * [People.Read.All (Admin Only](https://docs.microsoft.com/en-us/graph/permissions-reference#people-permissions)
 * [email](https://docs.microsoft.com/en-us/graph/permissions-reference#openid-permissions)
 * [offline_access](https://docs.microsoft.com/en-us/graph/permissions-reference#openid-permissions)
 * [openid](https://docs.microsoft.com/en-us/graph/permissions-reference#openid-permissions)
 * [profile](https://docs.microsoft.com/en-us/graph/permissions-reference#openid-permissions)

The permissions can be enabled via the [Microsoft Application Registration Portal](https://apps.dev.microsoft.com).

## Configuration

Note that the contact synchronisation will NOT happen automatically every time a new contact is added to the third-party provider's address book. A full sync will happen once the user has created her account, and periodically once per day. The periodic update can be enabled or disabled via the `com.openexchange.subscribe.autorun` server property.

Also note that this is an one-way sync, i.e. from the third-party provider towards the AppSuite and NOT vice versa.

Finally, ensure that in case of an upgrade to 7.10.2 you will need to generate new access tokens. More information [here](#upgrade-to-microsoft-graph-api).

# Microsoft OneDrive

To setup the Microsoft OneDrive file store you have to install the package `open-xchange-file-storage-onedrive`.

## Required Permissions

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
 
 Ensure that in case of an upgrade to 7.10.2 you will need to generate new access tokens. More information [here](#upgrade-to-microsoft-graph-api).