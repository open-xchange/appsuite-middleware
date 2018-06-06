---
title: Session Management
---

The session management feature allows users to get an overview of all connected clients which have an active session, remove/terminate certain sessions or remove/terminate all sessions except the current one.

# Configure session management

## Global lookup
By default, all existing user sessions in the (hazelcast) cluster are shown. This can be disabled by setting ```com.openexchange.session.management.globalLookup``` to ```false```. Deactivating global lookup is recommended for setups with only one middleware node or setups without installed ```open-xchange-sessionstorage-hazelcast``` package.

## Client blacklist
It's possible to blacklist client-identifiers, those sessions are not displayed for the user. This is recommended for clients that open user sessions for maintenance reasons. By default, the blacklist is empty. To blacklist clients, their client-identifiers have to be added to ```com.openexchange.session.management.clientBlacklist``` as comma-separated list.

## Use geolocation service (optional)
With an active geolocation service, in the sessions overview the user gets information about the location, based on the IP address assigned to that session. To enable geolocation service, the package ```open-xchange-geoip``` has to be installed. This package uses the geolocation service provided by MaxMind Inc. (www.maxmind.com), no further configuration of this service is needed.
