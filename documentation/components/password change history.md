---
Title: Password change history
---

# Introduction
With v7.8.4 the Open-Xchange Server offers a service to track and list the password changes made by a user. Following data is saved:

* The context identifier
* The user identifier
* The time the password was changed
* The client ID which changed the password 
* The IP address of the client that changed the password

Neither the old nor the new password are saved by this feature.


# Installation
The Password Change History feature is included in ``open-xchange-core`` package. Thus no additional packages need to be installed.

# Configuration
The Password Change History features needs to be enabled to run (Default with 7.10.0). Additional properties for a custom handler and a limitation of entries are described in [configuration section](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/#mode=features&feature=PasswordChangeHistory).
