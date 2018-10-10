---
title: Health-check for middleware
---

# Introduction
With v7.10.1 the Open-Xchange middleware offers health-checks to verify the middleware is running fine or get information which component is in trouble. This information can be retrieved via a REST interface.

# Installation
The health-check feature is included in ``open-xchange-core`` package. Thus, no additional packages need to be installed. The REST interface will be available via 
```
GET hostname:8009/health
```
.


# Configuration

## Enable authentication to access REST interface
By default, there is no authentication for accessing the REST interface. To enable authentication, the properties ``com.openexchange.health.username`` and ``com.openexchange.health.password`` must be set.

## Skip health-checks
To disable too expensive or invasive health-checks based on their names, the comma-separated list ``com.openexchange.health.skip`` can be set. Health-checks listed here will not be executed at all.

## Ignore health-check results
To ignore health-check results based on their names when evaluating the overall status, the comma-separated list ``com.openexchange.health.ignore`` can be set. Health-checks listed here will be executed and their results will be included in the response, but their status will not affect the overall status.


# Response
## Available health-checks
With ``open-xchange-core`` the following health-checks are available

### allPluginsLoaded
Checks if all bundles are ``ACTIVE``.
```json
{
    "name": "allPluginsLoaded",
    "status": "UP"
}
```

### jvmHeap
JVM heap metrics based on ``MemoryMXBean``-data. The field ``lastOOM`` is only available in ``DOWN`` case.
```json
{
    "name": "jvmHeap",
    "status": "UP",
    "data": {
        "init": "260046848",
        "max": "523501568",
        "used": "234073864",
        "commited": "345624576",
        "lastOOM": "2018-08-30T12:43:50,319+0200"
    }
}
```

### configDB
Check the round-trip-time for read/write connections to the configDB.
```json
{
    "name": "configDB",
    "status": "UP",
    "data": {
        "writeConnectionRoundTripTime": "230.573ms",
        "readConnectionRoundTripTime": "241.344ms"
    }
}
```

### hazelcast
If available, check for hazelcast cluster state.
```json
{
    "name": "hazelcast",
    "status": "UP",
    "data": {
        "clusterState": "ACTIVE",
        "clusterVersion": "3.10",
        "memberVersion": "3.10.5",
        "memberCount": "1",
        "isLiteMember": "false"
    }
}
```

## Service data
Some information about the installed middleware.
```json
"service":
    {
        "name": "appsuite-middleware",
        "version": "7.10.1-Rev0",
        "buildDate": "release-7.10.0",
        "date": "2018-09-29T16:23:58,524+0200",
        "timeZone": "Europe/Berlin",
        "locale": "de_DE",
        "charset": "UTF-8"
    }
```

## Response codes
If all health-checks are running fine (or ignored), the HTTP status will be ``200 OK`` and the health-check data will be included in the response body.

If any health-check is not running fine and not ignored, the HTTP status will be ``503 Service unavailable`` and the health-check data will be included in the response body.

In case of severe middleware problems and the health-checks can not be executed, the HTTP status will be ``500 Internal Server Error``. In this case no data can be included in the response body.
