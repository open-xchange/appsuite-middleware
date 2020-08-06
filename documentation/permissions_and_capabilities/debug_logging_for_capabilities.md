---
title: Debug logging for set of effective capabilities
icon: fa-lock
tags: Permissions, Capabilities
---

# Introduction

Since version v7.10.4 the Open-Xchange Middleware offers the feature to log the individual components that built up the set of effective capabilities associated with a certain user. This is useful to understand why a certain capability is absent (although somehow set) or available (although shouldn't).

The logging happens on user's first login when his/her set of effective capabilities is compiled or when manually invoking `getuserconfigurationsource` command-line tool.

# Enable `DEBUG` logging

To set-up logging for a certain user, please use `logconf` command-line tool and enable `DEBUG` logging for class with name `com.openexchange.capabilities.internal.AbstractCapabilityService`. Example:

```
logconf -u 3 -c 123 -a -l com.openexchange.capabilities.internal.AbstractCapabilityService=DEBUG -A oxadminmaster -P secret
```

and to remove `DEBUG` logging:

```
logconf -u 3 -c 123 -d -l com.openexchange.capabilities.internal.AbstractCapabilityService -A oxadminmaster -P secret
```

Moreover, `DEBUG` logging for capability sets can be globally enabled through modifying file `/opt/open-xchange/etc/logback.xml` and inserting the following line to the end:

```
<logger name="com.openexchange.capabilities.internal.AbstractCapabilityService" level="DEBUG"/>
```