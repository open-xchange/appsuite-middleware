---
title: Session management
---

# Introduction
With v7.10.0 the Open-Xchange Server offers the user a way to check what other sessions are already active based on his credentials. Information shown per session consists of IP address (and location if IP-based geolocation service is active), time of login, client and user-agent associated to this session. Also the user can terminate any of his sessions, e.g. in case a device gets stolen.

# Installation
This feature is included in ``open-xchange-core`` package. Thus, no additional packages are required being installed.

# Configuration

## Global lookup
By default, all existing user sessions in the (hazelcast) cluster are shown. This can be disabled by setting ```com.openexchange.session.management.globalLookup``` to ```false```. Deactivating global lookup is recommended for setups with only one middleware node or setups without installed ```open-xchange-sessionstorage-hazelcast``` package.

## Client blacklist
It's possible to blacklist client-identifiers, those sessions are not displayed for the user. This is recommended for clients that open user sessions for maintenance reasons. By default, the blacklist is empty. To blacklist clients, their client-identifiers have to be added to ```com.openexchange.session.management.clientBlacklist``` as comma-separated list.

## Use geolocation service (optional)
With an active geolocation service, in the sessions overview the user gets information about the location, based on the IP address assigned to that session. To enable geolocation service, the package ```open-xchange-geoip``` has to be installed. This package uses the geolocation service provided by MaxMind Inc. (www.maxmind.com), no further configuration of this service is needed.
