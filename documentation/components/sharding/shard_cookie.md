---
title: Shard Cookie
---

# Introduction
To route HTTP requests to their correct App Suite shard, an additional cookie is needed to identify the corresponding server. As most requests are authenticated and coupled to a session, the sharding cookie is aligned with usual session cookies. The cookies name is `open-xchange-shard`.

# Lifecycle
The cookie is set after login with the same parameters as the other session cookies, except for the value. The value is loaded from the server configuration and `default` if not set otherwise. On every session validation the cookies existence and value is also verified. As long as a valid session exists, this cookie will also exist and be recreated if absent or the value will be adjusted if it differs from the current configuration. The cookie is deleted alongside all other session related cookies or on expiry.

# Configuration
The value of the cookie can be configured by a lean and reloadable property, `com.openxchange.server.shardName` whose default value is `'default'`. This property is part of the server configuration and should reside in the `server.properties` file.