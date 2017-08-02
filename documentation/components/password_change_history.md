---
Title: Password change history
---

# Introduction
With v7.10.0 the Open-Xchange Server offers a service to track and list the password changes made by a user. The context and user ID, the time the password was changed, the client ID which changed the password and if available the IP address of the client is saved. The password itself does not get saved.


# Installation
This feature is included in ``open-xchange-core`` package. Thus, no additional packages are required being installed.

# Configuration
The password change history features needs to be enabled to run. Additional properties for a custom handler and a limitation of entries are described in [configuration section](http://documentation.open-xchange.com/components/middleware/config/{{version}}/index.html#mode=features&feature=PasswordChangeHistory).
