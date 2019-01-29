---
title: Microsoft Contacts
classes: toc
icon: fa-windows
---

# Required Permissions

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

# Configuration

Note that the contact synchronisation will NOT happen automatically every time a new contact is added to the third-party provider's address book. A full sync will happen once the user has created her account, and periodically once per day. The periodic update can be enabled or disabled via the `com.openexchange.subscribe.autorun` server property.

Also note that this is an one-way sync, i.e. from the third-party provider towards the AppSuite and NOT vice versa.

Finally, ensure that in case of an upgrade to 7.10.2 you will need to generate new access tokens. More information [here]({{ site.baseurl }}/middleware/components/oauth/microsoft.html#upgrade-to-microsoft-graph-api).