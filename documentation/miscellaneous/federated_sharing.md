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

Doing so, from the client perspective, there's almost no difference working with federated shares fee     

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


# Installation & Configuration

The new "Federated Sharing" feature is part of of the package ``open-xchange-subscribe``. For each possible kind of integration in the 
available modules, separate storage services or providers are shipped, through which access to the data of the guest accounts on the remote 
server or context is managed. 

The following gives an overview about how to enable these storage integrations for users.

## Cross-Context File Storage

...

## Cross-OX File Storage

...

## Cross-Context Calendar

...


# Restriction & Limitations 

Federated shares can be integrated into App Suite in a way that collaboration is not much different to regular, context-internal shares. 
However, there are a few limitations: 

- User needs general access to the module
- User needs "groupware" module permissions (for shared/public tree)
- User needs mail, to receive invitation mails
- Named guest users, only 
- No anonymous links
- Can only subscribe shares if invited guest's email matches alias 
- No free/busy, no adding of resources in cross-context calendar  
- No re-sharing (guests don't have "invite-guests" capability)
- ...

