---
title: Session management
---

# Introduction
With v7.10.0 the Open-Xchange Server offers the user a way to check what other sessions are already active based on his credentials. Information shown per session consists of IP address (and location if IP-based geolocation service is active), time of login, client and user-agent associated to this session. Also the user can terminate any of his sessions, e.g. in case a device gets stolen.

# Installation
This feature is included in ``open-xchange-core`` package. Thus, no additional packages are required being installed.

# Configuration
The session management feature is active by default. It is possible to enable/disable a global session lookup in session storage (`"com.openexchange.session.management.globalLookup"`) and to define a client blacklist (`"com.openexchange.session.management.clientBlacklist"`), to exclude clients from session listing (e.g. administrative sessions created by monitoring or other tools). Both properties are explained in the [configuration section](/components/middleware/config{{ site.baseurl }}/index.html#mode=features&feature=Sessionmanagement).
