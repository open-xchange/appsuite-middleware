---
title: HTTP Client configuration
icon: fa fa-exchange
tags: Administration, HTTP Client, Monitoring
---

This article will show an example on how you can configure the HTTP clients the Middleware uses to make requests to third parties. 


# Overview

Starting with version 7.10.4 of the Open Xchange Server it is possible to configure individual timeouts, etc. for HTTP clients used by the Middleware for request to third parties. 
This can either be done by setting general properties for all HTTP clients or by setting properties for each client. 
The follwing chapter will guide you through the configuration.

# Configuration

To guarantee backwards compatibility with older versions, different levels of configuration were introduced. 
The first level is introduced with this feature

## Level 1 - General configuration

To be able to configure all HTTP clients without a specific namespace, general properties were introduced. 
Those general properties will begin with `com.openenexchange.httpclient` and can be extended with the property to set. 
Following list covers all properties that can be set, with their default values:

```
com.openenexchange.httpclient.readTimeout=30000
com.openenexchange.httpclient.connectionTimeout=30000
com.openenexchange.httpclient.requestTimeout=30000
com.openenexchange.httpclient.keepAlive.duration=20
com.openenexchange.httpclient.keepAlive.monitorInterval=5
com.openenexchange.httpclient.totalConnections=20
com.openenexchange.httpclient.connectionPerRoute=10
com.openenexchange.httpclient.socketBufferSize=8192
```
 
Properties defined with this manner will be applied to all HTTP clients configuration.
>Note: For draw backs read on.

## Level 2 - Legacy properties

Previous versions of the Open Xchange Server had problematically defaults for HTT clients. 
These defaults will still be applied on top of the default configuration, efficiently overwrite the defaults set with `com.openenexchange.httpclient.*`.
This will avoid that HTTP clients will change their behavior when updating from a version 7.10.3 or earlier.
To make those problematically defaults visible, they got documented [here](https://documentation.open-xchange.com/components/middleware/config/latest/#mode=features&feature=HTTP%20Client%20Configuration%20legacy%20values)

Further previous version of the Open Xchange Server have already properties configuring HTTP clients. 
Just to given an example, the configuration for the iCAL feed feature can be found [here](https://documentation.open-xchange.com/components/middleware/config/latest/#mode=tags&tag=ICal%20Calendar%20Provider). 
These properties will stay untouched and will overwrite the defaults set with {{com.openenexchange.httpclient.*}}, like described above, too.

## Level 3 - Client specific configuration

To be able to configure special clients, it possible to add a client identifier to `com.openenexchange.httpclient.*`. 
Frankly speaking, it is possible to configure clients with e.g. `com.openenexchange.httpclient.icalfeed.*`, overwriting the default configuration. 
This specialized configuration always "wins". It will be applied on top of all other configuration.
The list of all available client identifier can be found below.


### HTTP client identifier

Following clients can be configured:

| Service name | Identifier |
|--------------|:----------:|
| Autoconfiguration ISPDB| autoconfig-ispdb |
| Autoconfiguration configuration server | autoconfig-server |
| DAV Push | davpush |
| DAV Subscribe | davsub |
| Dovecot Admin | dovadm |
| Dropbox | dropbox |
| iCAL Feed | icalfeed |
| Microsoft Graph | msgraph |
| Proxy | proxy|
| RestExecutor | rest |
| SAML OAuth | saml-oauth |
| Schedjoules | schedjoules |
| Sipgate | sipgate |
| Spam experts | spamexperts |
| SProxyD | sproxyd-[filestoreID] |
| XING | xing |


# Examples

## Example 1: Adjust the common configuration

To adjust configuration for all clients, expect with the draw backs from above, it is only required to set


```
com.openenexchange.httpclient.readTimeout=30000
com.openenexchange.httpclient.connectionTimeout=30000
com.openenexchange.httpclient.requestTimeout=30000
com.openenexchange.httpclient.keepAlive.duration=20
com.openenexchange.httpclient.keepAlive.monitorInterval=5
com.openenexchange.httpclient.totalConnections=20
com.openenexchange.httpclient.connectionPerRoute=10
com.openenexchange.httpclient.socketBufferSize=8192
```


## Example 2: Adjust configuration for iCAL feeds

To adjust configuration for example the HTTP client for iCAL feeds on top of the general configuration, use the identifier 
`icalfeed` to enhance the configuration to your needs. 
>Note: The following example still uses the default values for the iCAL feed HTTP client. 

```

com.openenexchange.httpclient.readTimeout=30000
com.openenexchange.httpclient.connectionTimeout=30000
com.openenexchange.httpclient.requestTimeout=30000
com.openenexchange.httpclient.keepAlive.duration=20
com.openenexchange.httpclient.keepAlive.monitorInterval=5
com.openenexchange.httpclient.totalConnections=20
com.openenexchange.httpclient.connectionPerRoute=10
com.openenexchange.httpclient.socketBufferSize=8192

com.openenexchange.httpclient.icalfeed.readTimeout=30000
com.openenexchange.httpclient.icalfeed.connectionTimeout=30000
com.openenexchange.httpclient.icalfeed.requestTimeout=30000
com.openenexchange.httpclient.icalfeed.keepAlive.duration=20
com.openenexchange.httpclient.icalfeed.keepAlive.monitorInterval=5
com.openenexchange.httpclient.icalfeed.totalConnections=20
com.openenexchange.httpclient.icalfeed.connectionPerRoute=10
com.openenexchange.httpclient.icalfeed.socketBufferSize=8192
```


## Example 3: Adjust configuration for iCAL feeds with legacy properties set 

In addition to these configurations, some features already had possibilities to adjust HTTP client configuration, just like the iCAL feed feature from above. 
In such a case the legacy properties will still be used, if not configured otherwise with the new properties.

```
# Legacy configuration in calendar.properties file
com.openexchange.calendar.ical.maxConnections=10
com.openexchange.calendar.ical.maxConnectionsPerRoute=5
com.openexchange.calendar.ical.connectionTimeout=50000
com.openexchange.calendar.ical.socketReadTimeout=50000

# New configuration for iCAL feeds
com.openenexchange.httpclient.icalfeed.readTimeout=30000
com.openenexchange.httpclient.icalfeed.requestTimeout=30000
com.openenexchange.httpclient.icalfeed.keepAlive.duration=20
com.openenexchange.httpclient.icalfeed.keepAlive.monitorInterval=5
com.openenexchange.httpclient.icalfeed.totalConnections=20
com.openenexchange.httpclient.icalfeed.socketBufferSize=8192

```

As you may have already noticed, `com.openenexchange.httpclient.icalfeed.connectionTimeout` and 
`com.openenexchange.httpclient.icalfeed.connectionPerRoute` have not been set. 
Instead of falling back to the default values, the values from `com.openexchange.calendar.ical.connectionTimeout` and 
`com.openexchange.calendar.ical.maxConnectionsPerRoute` will be used for the client configuration. 
Further the values for the legacy properties `com.openexchange.calendar.ical.maxConnections` and 
`com.openexchange.calendar.ical.socketReadTimeout` will **be overwritten**  by the new properties 
`com.openenexchange.httpclient.icalfeed.totalConnections` and `com.openenexchange.httpclient.icalfeed.readTimeout`.

Efficiently the configuration from above can be written like this

```
com.openenexchange.httpclient.icalfeed.readTimeout=30000
com.openenexchange.httpclient.icalfeed.connectionTimeout=50000
com.openenexchange.httpclient.icalfeed.requestTimeout=30000
com.openenexchange.httpclient.icalfeed.keepAlive.duration=20
com.openenexchange.httpclient.icalfeed.keepAlive.monitorInterval=5
com.openenexchange.httpclient.icalfeed.totalConnections=20
com.openenexchange.httpclient.icalfeed.connectionPerRoute=5
com.openenexchange.httpclient.icalfeed.socketBufferSize=8192
```

It is *not recommended* to use this mixed configuration setup.

# Legacy properties

Following features did already provide a possibility to configure HTTP clients. 
These legacy properties will be *preferred* over common properties starting with `com.openenexchange.httpclient.*`

| Service name | Identifier | Legacy property name | 
|--------------|:-----------|:--------------------:|
| Dovecot Admin | dovadm | `com.openexchange.dovecot.doveadm.endpoints.doveadm.*` |
| iCAL Feed | icalfeed | `com.openexchange.calendar.ical.*` |
| SAML OAuth | saml-oauth | `com.openexchange.saml.oauth*` |
| SProxyD | sproxyd-[filestoreID] | `com.openexchange.filestore.sproxyd.[fielstoreID].*` |


# Further notes

Please note that the client will only have the in- or decreased resources if the **server is restarted** or **the configuration has been reloaded**. 
In case of a reload, the HTTP clients will be renewed. This might cause for a short time an increased memory demand.

Please also note, that if no values are set, programmatically default will be used. In case legacy properties are set, those are still preferred.
