---
title: Metrics Overview
icon: fas fa-chart-bar
tags: Monitoring, Administration
---

# Introduction

The OX Appsuite publishes all metrics in the prometheus format under the /metrics rest endpoint. This page provides an overview of available metrics.

# Metric overview

| Name                                         | Tags                | Package                   | Additional info         | Required configuration |
|:---------------------------------------------|:--------------------|:--------------------------|:------------------------|:----------------------:|
| appsuite_s3_requestTimes_<HTTP_METHOD_NAME\> || open-xchange-filestore-s3 | Replace the <HTTP_METHOD_NAME> with the corresponding http method (e.g. 'GET' or 'PUT'). | [property](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/#mode=search&term=com.openexchange.filestore.s3.metricCollection) |
| appsuite_s3_requestSizeTimer                 | type | open-xchange-filestore-s3 ||[property](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/#mode=search&term=com.openexchange.filestore.s3.metricCollection) |
| appsuite_s3_requestSize                      | type | open-xchange-filestore-s3 ||[property](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/#mode=search&term=com.openexchange.filestore.s3.metricCollection) |
| appsuite_ipcheck_totalIPChanges              | | open-xchange-core |||
| appsuite_ipcheck_acceptedIPChanges           | | open-xchange-core |||
| appsuite_ipcheck_deniedIPChanges             | | open-xchange-core |||
| appsuite_ipcheck_acceptedPrivateIP           | | open-xchange-core |||
| appsuite_ipcheck_acceptedWhiteListed         | | open-xchange-core |||
| appsuite_ipcheck_acceptedEligibleIPChanges   | | open-xchange-core |||
| appsuite_ipcheck_deniedException             | | open-xchange-core |||
| appsuite_ipcheck_deniedCountryChanged        | | open-xchange-core |||
| appsuite_sessions_active_count               | client=`[all|clientname]` | open-xchange-core |||
| appsuite_sessions_total_count                | client=`[all|clientname]` | open-xchange-core |||
| appsuite_sessions_short_term_count           | client=`[all|clientname]` | open-xchange-core |||
| appsuite_sessions_long_term_count            | client=`[all|clientname]` | open-xchange-core |||
| appsuite_cache_events_offered                | region=`[all|cacheRegion]` |  open-xchange-core | Offered events for the specified cache region ||
| appsuite_cache_events_delivered              | region=`[all|cacheRegion]` | open-xchange-core | Delivered events for the specified cache region ||
| appsuite_antivirus_cache_hit                 | | open-xchange-antivirus |||
| appsuite_antivirus_cache_miss                | | open-xchange-antivirus |||
| appsuite_antivirus_cache_invalidations       | | open-xchange-antivirus |||
| appsuite_antivirus_scanning_rate             | | open-xchange-antivirus |||
| appsuite_antivirus_scanning_time             | | open-xchange-antivirus |||
| appsuite_antivirus_transfer_size             | | open-xchange-antivirus |||
| appsuite_imap_commands                       | cmd, status, host | open-xchange-imap |||
| appsuite_sproxyd_EndpointPool_TotalSize      | filestore | open-xchange-filestore-sproxyd |||
| appsuite_sproxyd_EndpointPool_Available      | filestore | open-xchange-filestore-sproxyd |||
| appsuite_sproxyd_EndpointPool_Unavailable    | filestore | open-xchange-filestore-sproxyd |||
| appsuite_httpapi_requests_seconds            | action=`[all|actionName]`,module,status | open-xchange-core |||
| jvm_*                                        | * | open-xchange-core | A collection of java virtual maschine metrics ||
| appsuite_executor_completed_tasks            | name | open-xchange-core |||
| appsuite_executor_active_threads             | name | open-xchange-core |||
| appsuite_executor_queued_tasks               | name | open-xchange-core |||
| appsuite_executor_queue_remaining_tasks      | name | open-xchange-core |||
| appsuite_executor_pool_size_threads          | name | open-xchange-core |||


