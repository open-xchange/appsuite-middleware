---
title: Prometheus migration
icon: fas fa-chart-line
tags: Administration, Monitoring
---

Version 7.10.4 of the appsuite middleware introduces some changes for the monitoring system. Up to now metrics are gathered via the MetricsService interface and it was usually implemented by the dropwizard default implementation which registered a bean for each metric. Because this is not state of the art - especially in cloud environments - it was replaced by micrometer. Micrometer gathers all metrics and expose it via the single /metrics servlet. The servlet provides the metrics in the prometheus format. For now the old MetricService still exists but all core middleware metrics have been migrated to micrometer. During that process some of the metric names have been aligned with one another. If you plan to upgrade from a previous version to 7.10.4 you need to adjust you monitoring setup. For this purpose this page provides the informations about those changes.

# Metrics with changed names

| Name                                              | Package                   | New Name                | Additional info |
|:--------------------------------------------------|:--------------------------|:------------------------|:----------------------:|
| name=RequestTimes.<HTTP_METHOD_NAME\>,type=s3      | open-xchange-filestore-s3 |appsuite_s3_requestTimes_<HTTP_METHOD_NAME\>|  |
| name=S3UploadThroughput,type=s3                   | open-xchange-filestore-s3 |appsuite_s3_requestSizeTimer\{type=<Type\>\} && appsuite_s3_requestSize\{type=<Type\>\}| Have been splitted into two metrics |
| name=S3DownloadThroughput,type=s3                 | open-xchange-filestore-s3 || See above. |
| name=Meter.totalIPChanges,type=ipcheck            | open-xchange-core |appsuite_ipcheck_totalIPChanges ||
| name=Meter.acceptedIPChanges,type=ipcheck         | open-xchange-core |appsuite_ipcheck_acceptedIPChanges||
| name=Meter.deniedIPChanges,type=ipcheck           | open-xchange-core |appsuite_ipcheck_deniedIPChanges||
| name=Meter.acceptedPrivateIP,type=ipcheck         | open-xchange-core |appsuite_ipcheck_acceptedPrivateIP||
| name=Meter.acceptedWhiteListed,type=ipcheck       | open-xchange-core |appsuite_ipcheck_acceptedWhiteListed||
| name=Meter.acceptedEligibleIPChanges,type=ipcheck | open-xchange-core |appsuite_ipcheck_acceptedEligibleIPChanges||
| name=Meter.deniedException,type=ipcheck           | open-xchange-core |appsuite_ipcheck_deniedException||
| name=Meter.deniedCountryChanged,type=ipcheck      | open-xchange-core |appsuite_ipcheck_deniedCountryChanged||
| client=all,name=ActiveCount,type=sessiond         | open-xchange-core |appsuite_sessions_active_count\{client=all\}||
| client=all,name=TotalCount,type=sessiond          | open-xchange-core |appsuite_sessions_total_count\{client=all\}||
| client=all,name=ShortTermCount,type=sessiond      | open-xchange-core |appsuite_sessions_short_term_count\{client=all\}||
| client=all,name=LongTermCount,type=sessiond       | open-xchange-core |appsuite_sessions_long_term_count\{client=all\}||
| name=<CacheRegionName\>.offeredEvents,type=cache   | open-xchange-core |appsuite_cache_events_offered\{region=<CacheRegionName\>\} ||
| name=<CacheRegionName\>.deliveredEvents,type=cache | open-xchange-core |appsuite_cache_events_delivered\{region=<CacheRegionName\>\} ||
| name=offeredEvents,type=cache                     | open-xchange-core |appsuite_cache_events_offered\{region=all\} ||
| name=deliveredEvents,type=cache                   | open-xchange-core |appsuite_cache_events_delivered\{region=all\} ||
| name=Cache Hit,type=antivirus                     | open-xchange-antivirus |appsuite_antivirus_cache_hit||
| name=Cache Miss,type=antivirus                    | open-xchange-antivirus |appsuite_antivirus_cache_miss||
| name=Cache Invalidations,type=antivirus           | open-xchange-antivirus |appsuite_antivirus_cache_invalidations||
| name=Scanning Rate,type=antivirus                 | open-xchange-antivirus |appsuite_antivirus_scanning_rate||
| name=Scanning Time,type=antivirus                 | open-xchange-antivirus |appsuite_antivirus_scanning_time||
| name=Transfer Rate,type=antivirus                 | open-xchange-antivirus |appsuite_antivirus_transfer_size||
| name=imap,type=requestRate,server=<HostName@Port\> | open-xchange-core |appsuite_imap_commands\{cmd=<Command\>,status=<Status\>,host=<host\>\}|Merged with errorRate|
| name=imap,type=errorRate,server=<HostName@Port\> | open-xchange-imap ||Merged with requestRate. See above.|
| name=sproxyd,type=EndpointPool.TotalSize,filestore=<FilestoreId\> | open-xchange-filestore-sproxyd | appsuite_sproxyd_EndpointPool_TotalSize\{filestore=<FilestoreId\>\} ||
| name=sproxyd,type=EndpointPool.Available,filestore=<FilestoreId\> | open-xchange-filestore-sproxyd | appsuite_sproxyd_EndpointPool_Available\{filestore=<FilestoreId\>\} ||
| name=sproxyd,type=EndpointPool.Unavailable,filestore=<FilestoreId\> | open-xchange-filestore-sproxyd | appsuite_sproxyd_EndpointPool_Unavailable\{filestore=<FilestoreId\>\} ||


# Reworked and new metrics

Some metrics have been reworked for various reasons or have been newly added. This is the list of feature with those metrics. Please see the respective feature documentation or a dedicated monitoring article for more informations:

* Circuit-breakers // TODO add link to docu
* Http api
* Rest api
* DB pool
* Soap api
* WebDAV api
* JCS cache

