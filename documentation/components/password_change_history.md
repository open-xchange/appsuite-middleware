---
title: Password change history
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
How to disable the feature, set a custom handler or a limitation of entries is described in the [configuration section](/components/middleware/config{{ site.baseurl }}/index.html#mode=features&feature=PasswordChangeHistory).
