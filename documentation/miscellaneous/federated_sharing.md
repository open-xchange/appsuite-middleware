---
title: Federated Sharing
icon: fa-share-alt-square
tags: Sharing, Configuration, Administration
---

# Introduction

With the introduction of the [Sharing and Guest Mode]({{ site.baseurl }}/middleware/miscellaneous/sharing_and_guest_mode.html)
in OX App Suite 7.8.0 external users, without a regular account on the server, are able to interact with the shared data in 
the same way as regular users do by using the guest user interface. However, if a guest user is also a regular user on another
OX App Suite server, or on the same server but in another context, jumping back and fourth between the regular and the guest 
user interface is quite cumbersome.

*OX App Suite 7.10.5* introduces the new concept of "*Federated Sharing*" which allows a user to integrate received shares from 
other contexts or servers into his own account. This provides a seamless integration of shared data and a smooth user experience,
as the remote data can be accessed in the same way as it would have been stored locally.


# Overview

Whenever something is shared to an *external* user, an email is sent to the recipient containing a share link that can be used to 
access the shared data in the guest mode of App Suite. 

These notification mails are now used as entry point for setting up a permanent subscription to the data shared to the guest user. 
Once such an email message is received in App Suite 7.10.5, it is analyzed and options to integrate the share into the user's own
App Suite interface are available. After the federated share is integrated, the shared files and folders from the remote server 
or context are seamlessly integrated into the local folder tree below *Shared Files* or *Public Files*. 

No data is copied or synchronized for federated shares, and no additional quota is accounted for subscriptions. Instead, the data
on the remote server is accessed live and directly, proxied through HTTP client integrated in the middleware. Data from other 
contexts is directly fetched from the storage. 

Doing so, from the client perspective, there's almost no difference working with federated shares.

## Available Modules

In the first iteration, shared folders from the modules *Drive* and *Calendar* can be integrated, where the latter one is only available
in *cross-context* mode (see below). This also includes existing shares originating from installations running a previous server version. 
Data shared from the other groupware modules (*Contacts* and *Tasks*) can still be accessed through the share link using the regular guest 
mode of App Suite. 

## Cross Context

When it comes to integrating a share, OX App Suite can handle two different modes: The *cross-context* and the *cross-ox* mode. The 
*cross-context* mode is chosen when a share comes from a different context on the same OX server. The server will use internal services 
to access the shared data in the other context. No remote communication is required in this case. 

The share link for the guest account is used to spawn a regular guest session in the system, so that any changes performed within a 
federated share are still performed by this guest account. 

## Cross OX

The *cross-ox* mode is used when a share is from *another* OX server. The communication between the OX servers will then take place over 
the HTTP API. The client will still interact with the local OX server exclusively, and the requests to files and folders of the federated 
share are proxied through the middleware. This also includes establishing a regular session on the remote server by logging in through the
original share link.

### HTTP API client

The *cross-ox* mode uses an HTTP client to establish and uphold the communication to the remote OX server. This client named *apiClient* and its mechanisms are described in more detail with further configuration options [here]({{site.baseurl}}middleware/administration/http_client_configuration.html).

# Installation & Configuration

The new "Federated Sharing" feature is part of of the package ``open-xchange-subscribe``. For each possible kind of integration in the 
available modules, separate storage services or providers are shipped, through which access to the data of the guest accounts on the remote 
server or context is managed. 

The following gives an overview about how to enable these storage integrations for users.

## Cross-Context File Storage

The cross-context file storage is registered in the middleware as separate file storage service. Within the service, separate accounts are 
managed for each linked guest user in the remote context. The file storage service can be activated for end users by enabling the 
corresponding capability ``filestorage_xctx``. Once enabled, users will be offered the options to subscribe to shares received from other 
contexts of the installation when the invitation mail is opened in the App Suite user interface. 

In cross-context mode, the integrated share's base token is used to resolve the *remote* context in the installation internally, whose data 
may be located in another database schema and/or file store. Access to the underlying services is performed using internal interfaces, and 
not using the HTTP API, in contrast to cross-ox shares.

Besides the capability to enable/disable cross-context shares in module Drive, the 
[configuration documentation](https://documentation.open-xchange.com/components/middleware/config{{site.baseurl}}#mode=search&term=com.openexchange.file.storage.xctx.)
lists further properties to control the behavior of the file storage service.

## Cross-OX File Storage

Also the file storage to integrate shares from foreign servers is registered in the middleware as separate file storage service. To allow the
creation of accounts linked to guest users on other OX servers, the capability ``filestorage_xox`` can be enabled. Afterwards, users are able 
to subscribe to shared received from other servers when the invitation mail is opened in the App Suite user interface. 

Data on the remote server is accessed using an HTTP client integrated into the middleware, which basically proxies incoming requests from the 
local client to the remote server and marshalls back the responses. Therefore, the guest session is initiated through the initial share link, and
managed internally afterwards. 

Further settings for the file storage service for cross-ox shares are listed over at the 
[configuration documentation](https://documentation.open-xchange.com/components/middleware/config{{site.baseurl}}#mode=search&term=com.openexchange.file.storage.xox.). 
Characteristics of the internal HTTP client can be fine tuned through 
[these properties](https://documentation.open-xchange.com/components/middleware/config{{site.baseurl}}#mode=search&term=com.openexchange.api.client.); 
furthermore, the general options to influence the HTTP client connections are available as described at the 
[HTTP Client Configuration]({{ site.baseurl }}/middleware/administration/http_client_configuration.html) article.

## Cross-Context Calendar

Similarly as the file storage services are managing accounts for subscribed federated shares, there's also a separate calendar provider for 
cross-context shares. The provider can be enabled for end users using the capability ``calendar_xctx2`` which is usually done through the 
configuration property <code>[com.openexchange.calendar.xctx2.enabled](https://documentation.open-xchange.com/components/middleware/config{{site.baseurl}}#com.openexchange.calendar.xctx2.enabled)</code>
Then, if an invitation mail to a calendar located in another context of the installation is displayed in App Suite, an option to subscribe to 
the share will be offered. 

Just like the cross-context file storage service, access to subscribed calendars is initiated through the share URL's base token which translates 
to the *remote* context on the installation using regular internal services. No additional HTTP client requests are performed.

Besides the capability to enable/disable cross-context shares in module Calendar, the 
[configuration documentation](https://documentation.open-xchange.com/components/middleware/config{{site.baseurl}}#mode=search&term=com.openexchange.calendar.xctx2.)
lists further properties to control the behavior of the calendar provider.


# Further Details

## Linked Guest Accounts

The guest mode of App Suite works by making all data that is shared to a certain guest user (as identified by its email address) available in an 
aggregated way. That means that a guest user will always have access to all data that has been shared to him from the originating context 
within the same interface and session, regardless which share link he previously followed to open the App Suite client. 

In a similar way, after a cross-context or cross-ox subscription has been established for a share, all data from the same module the guest user 
has access to will also be available automatically as federated share in App Suite. For example, if two users from a context share their folders 
to the same external user, and this external user subscribes to any of those shares in *his* App Suite, both folders will be available for him 
automatically. Of course it is still possible to unsubscribe from them again later. Also, if a previously shared folder is no longer accessible for 
the guest user (as it has either been deleted or permissions have been revoked), it will no longer appear as federated share.

After a guest account has been integrated as federated share, this linked relationship will be kept up as long as the remote guest account exists. 
As remote guest accounts are purged automatically after a configurable expiration time from after the last share has been revoked, the same is done
on the subscribing side with the federated share. This default behavior may be adjusted using 
[these properties](https://documentation.open-xchange.com/components/middleware/config{{site.baseurl}}#mode=search&term=.autoRemoveUnknownShares). 

Additionally, the user is able to remove the federated sharing account on his own whenever the last folder is unsubscribed. Afterwards, it is still 
possible to re-link the guest account from the invitation mail. 


## Guest Sessions

Data of subscribed shares is always accessed under the perspective of the corresponding guest user account. In order to do so, an appropriate 
session for the guest user is spawned in the remote context or server dynamically. 

Therefore, for each integrated share, the original share URL is stored within the corresponding account's configuration data, after it was set up 
through the original share invitation mail. In case the guest account is secured with a password, this password can also be stored within the 
associated subscription, secured with the secrets of the local session's user. 

Whenever needed, this share link (or rather its base token) is used to log in to the guest account and acquire a session on the remote context or 
server, just like it would happen when opening the share link in a browser. This also means that the same restrictions and constraints regarding the
maximum number of guest sessions or their default lifetime are still valid. Within the middleware, such spawned guest sessions are preserved and 
re-used while when interacting with the shared data, and automatically time out if not used for a while. 


## Proxy Access & Impersonation

When acting within an integrated federated share, the user implicitly impersonates as the guest user on the remote context or server. This means that 
all changes performed in the guest account are effectively performed by the guest user, and this is how they appear for the sharing user in the 
original context. This is why it is only possible to subscribe to shares that are addressed to the user's email directly (or one of its registered 
aliases), and these options won't be available for forwarded invitation mails for example. 

The data from the remote context or server is always requested directly and no synchronization takes place, so that any changes performed on the 
one side will directly appear at the other. That also means that no additional quota is accounted for subscribed shares, and the same limitations as
when opening the share in the guest interface of App Suite apply.


## Restriction & Limitations 

Federated shares can be integrated into App Suite in a way that collaboration is not much different to regular, context-internal shares. 
However, there are a few limitations: 

* In order to integrate an external share, the user needs to have the corresponding module permissions (e.g. ``infostore`` or `calendar`)
* To subscribe to shared folders, users need to have appropriate *groupware* module permissions (``readcreatesharedfolders``)
* Federated shares are created in App Suite through invitation mails, so the ``webmail`` module permissions has to be granted
* It is only possible to subscribe to shares for named guest users, i.e. *anonymous links* cannot be integrated as federated share
* Only guest users with an email address matching the user's email address (or an alias) can be integrated
* It's not possible to subscribe to single file shares, i.e. only shared folders will be appear as federated share
* Drive data from integrated federated shares won't be available when accessing the server via WebDAV
* Drive data from integrated federated shares won't be available for Drive synchronization clients
* No real-time collaboration is possible when editing *remote* documents located in subscribed federated shares
* The implicit conflict check prior creating/updating events will only be performed for attendees in the targeted remote context
* It is not possible to add *local* resource or group attendees to events created in *remote* calendars of federated shares 
* Calendars from integrated federated shares won't be available when accessing the server via CalDAV
* No re-sharing of subscribed shares is possible, since guest users don't have *invite_guests* capability
* No push notifications for the web client are triggered for changes in integrated cross-context calendars
* Depending on the version the remote server is running in *cross-ox* mode, certain features may not be available when interacting with the share
