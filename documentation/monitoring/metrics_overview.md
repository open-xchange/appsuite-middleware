---
title: Metrics Overview
icon: fas fa-chart-bar
tags: Monitoring, Administration
---

# Introduction

The OX Appsuite publishes new metrics within the com.openexchange.metrics domain. This page provides an overview of available metrics.
You can access a list of those metrics with descriptions via jolokia. E.g. for localhost:

```
curl http://yourname:yourpassword@localhost:8009/monitoring/jolokia/list/com.openexchange.metrics
```

Further information about how to setup jolokia can be found [here](jolokia.html).

To access the value of a metric use:

```
curl http://yourname:yourpassword@localhost:8009/monitoring/jolokia/read/com.openexchange.metrics:<metric_name>
```

# Metric overview

| Name                                              | Package                   | Additional info         | Required configuration |
|:--------------------------------------------------|:--------------------------|:------------------------|:----------------------:|
| name=RequestTimes.<HTTP_METHOD_NAME>,type=s3      | open-xchange-filestore-s3 | The <HTTP_METHOD_NAME> must be replaced with the corresponding http method (e.g. 'GET' or 'PUT'). | [property](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/#mode=search&term=com.openexchange.filestore.s3.metricCollection) |
| name=S3UploadThroughput,type=s3                   | open-xchange-filestore-s3 ||[property](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/#mode=search&term=com.openexchange.filestore.s3.metricCollection) |
| name=S3DownloadThroughput,type=s3                 | open-xchange-filestore-s3 ||[property](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/#mode=search&term=com.openexchange.filestore.s3.metricCollection) |
| name=Meter.totalIPChanges,type=ipcheck            | open-xchange-core |||
| name=Meter.acceptedIPChanges,type=ipcheck         | open-xchange-core |||
| name=Meter.deniedIPChanges,type=ipcheck           | open-xchange-core |||
| name=Meter.acceptedPrivateIP,type=ipcheck         | open-xchange-core |||
| name=Meter.acceptedWhiteListed,type=ipcheck       | open-xchange-core |||
| name=Meter.acceptedEligibleIPChanges,type=ipcheck | open-xchange-core |||
| name=Meter.deniedException,type=ipcheck           | open-xchange-core |||
| name=Meter.deniedCountryChanged,type=ipcheck      | open-xchange-core |||
| client=all,name=ActiveCount,type=sessiond         | open-xchange-core |||
| client=all,name=TotalCount,type=sessiond          | open-xchange-core |||
| client=all,name=ShortTermCount,type=sessiond      | open-xchange-core |||
| client=all,name=LongTermCount,type=sessiond       | open-xchange-core |||
| name=<CacheRegionName>.offeredEvents,type=cache   | open-xchange-core | Offered events for cache region "<CacheRegionName>" ||
| name=<CacheRegionName>.deliveredEvents,type=cache | open-xchange-core | Delivered events for cache region "<CacheRegionName>" ||
| name=offeredEvents,type=cache                     | open-xchange-core | Offered events for all cache regions ||
| name=deliveredEvents,type=cache                   | open-xchange-core | Delivered events for all cache regions ||
| name=Cache Hit,type=antivirus                     | open-xchange-antivirus |||
| name=Cache Miss,type=antivirus                    | open-xchange-antivirus |||
| name=Cache Invalidations,type=antivirus           | open-xchange-antivirus |||
| name=Scanning Rate,type=antivirus                 | open-xchange-antivirus |||
| name=Scanning Time,type=antivirus                 | open-xchange-antivirus |||
| name=Transfer Rate,type=antivirus                 | open-xchange-antivirus |||
