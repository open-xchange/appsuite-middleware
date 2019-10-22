---
title: Password Change History
icon: fa-history
tags: Security, Configuration, Installation
---

# Introduction
With v7.8.4 the Open-Xchange Server offers a service to track and list the password changes made by a user. Following data is saved:

* The context identifier
* The user identifier
* The time the password was changed
* The client identifier which changed the password 
* The IP address of the client that changed the password

Neither the old nor the new password are saved by this feature. Since v7.10.0 the Password Change History features is enabled per default.


# Installation
The Password Change History feature is included in ``open-xchange-core`` package. Thus no additional packages need to be installed.

# Configuration
How to disable the feature, set a custom handler or a limitation of entries is described in the [configuration section](/components/middleware/config{{ site.baseurl }}/index.html#mode=tags&tag=Password%20Change%20History).


# REST API

The Password Change History can be access via a REST API. This API is shipped with the package ``open-xchange-admin``.

## Endoint

The API ist accessible via the URL
```
http://example.org/admin/v1/contexts/{context-id}/users/{user-id}/passwd-changes
```
Within the URL the context and the user identifier are embedded. Thus there is a unique URL for each user.


## Authentication

The API can be configured to be accessed by different roles. However the context administrator is always allowed to access the API, when transmitting her credentials via basic auth.

### By context property

If the property ``CONTEXT_AUTHENTICATION_DISABLED`` is set to ``TRUE`` access to the API is allowed ``without`` any validation.

### By master property

If the property ``MASTER_AUTHENTICATION_DISABLED`` is set to ``TRUE`` and the property ``MASTER_ACCOUNT_OVERRIDE`` is set to ``TRUE``, too, access to the API is allowed ``without`` any validation.